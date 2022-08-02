package io.papermc.paper.configuration;

import com.google.common.base.Suppliers;
import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.configuration.legacy.RequiresSpigotInitialization;
import io.papermc.paper.configuration.serializer.ComponentSerializer;
import io.papermc.paper.configuration.serializer.EnumValueSerializer;
import io.papermc.paper.configuration.serializer.FastutilMapSerializer;
import io.papermc.paper.configuration.serializer.PacketClassSerializer;
import io.papermc.paper.configuration.serializer.StringRepresentableSerializer;
import io.papermc.paper.configuration.serializer.TableSerializer;
import io.papermc.paper.configuration.serializer.collections.MapSerializer;
import io.papermc.paper.configuration.serializer.registry.RegistryHolderSerializer;
import io.papermc.paper.configuration.serializer.registry.RegistryValueSerializer;
import io.papermc.paper.configuration.transformation.Transformations;
import io.papermc.paper.configuration.transformation.global.LegacyPaperConfig;
import io.papermc.paper.configuration.transformation.world.FeatureSeedsGeneration;
import io.papermc.paper.configuration.transformation.world.LegacyPaperWorldConfig;
import io.papermc.paper.configuration.type.BooleanOrDefault;
import io.papermc.paper.configuration.type.DoubleOrDefault;
import io.papermc.paper.configuration.type.Duration;
import io.papermc.paper.configuration.type.EngineMode;
import io.papermc.paper.configuration.type.IntOrDefault;
import io.papermc.paper.configuration.type.fallback.FallbackValueSerializer;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.spigotmc.SpigotConfig;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;
import static io.leangen.geantyref.GenericTypeReflector.erase;

@SuppressWarnings("Convert2Diamond")
public class PaperConfigurations extends Configurations<GlobalConfiguration, WorldConfiguration> {

    private static final Logger LOGGER = LogUtils.getLogger();
    static final String GLOBAL_CONFIG_FILE_NAME = "paper-global.yml";
    static final String WORLD_DEFAULTS_CONFIG_FILE_NAME = "paper-world-defaults.yml";
    static final String WORLD_CONFIG_FILE_NAME = "paper-world.yml";
    public static final String CONFIG_DIR = "config";
    private static final String BACKUP_DIR ="legacy-backup";

    private static final String GLOBAL_HEADER = String.format("""
            This is the global configuration file for Paper.
            As you can see, there's a lot to configure. Some options may impact gameplay, so use
            with caution, and make sure you know what each option does before configuring.

            If you need help with the configuration or have any questions related to Paper,
            join us in our Discord or check the docs page.

            The world configuration options have been moved inside
            their respective world folder. The files are named %s

            Docs: https://docs.papermc.io/
            Discord: https://discord.gg/papermc
            Website: https://papermc.io/""", WORLD_CONFIG_FILE_NAME);

    private static final String WORLD_DEFAULTS_HEADER = """
            This is the world defaults configuration file for Paper.
            As you can see, there's a lot to configure. Some options may impact gameplay, so use
            with caution, and make sure you know what each option does before configuring.

            If you need help with the configuration or have any questions related to Paper,
            join us in our Discord or check the docs page.

            Configuration options here apply to all worlds, unless you specify overrides inside
            the world-specific config file inside each world folder.

            Docs: https://docs.papermc.io/
            Discord: https://discord.gg/papermc
            Website: https://papermc.io/""";

    private static final Function<ContextMap, String> WORLD_HEADER = map -> String.format("""
        This is a world configuration file for Paper.
        This file may start empty but can be filled with settings to override ones in the %s/%s
        
        World: %s (%s)""",
        PaperConfigurations.CONFIG_DIR,
        PaperConfigurations.WORLD_DEFAULTS_CONFIG_FILE_NAME,
        map.require(WORLD_NAME),
        map.require(WORLD_KEY)
    );

    private static final Supplier<SpigotWorldConfig> SPIGOT_WORLD_DEFAULTS = Suppliers.memoize(() -> new SpigotWorldConfig(RandomStringUtils.randomAlphabetic(255)) {
        @Override // override to ensure "verbose" is false
        public void init() {
            SpigotConfig.readConfig(SpigotWorldConfig.class, this);
        }
    });
    static final ContextKey<Supplier<SpigotWorldConfig>> SPIGOT_WORLD_CONFIG_CONTEXT_KEY = new ContextKey<>(new TypeToken<Supplier<SpigotWorldConfig>>() {}, "spigot world config");


    public PaperConfigurations(final Path globalFolder) {
        super(globalFolder, GlobalConfiguration.class, WorldConfiguration.class, GLOBAL_CONFIG_FILE_NAME, WORLD_DEFAULTS_CONFIG_FILE_NAME, WORLD_CONFIG_FILE_NAME);
    }

    @Override
    protected YamlConfigurationLoader.Builder createLoaderBuilder() {
        return super.createLoaderBuilder()
            .defaultOptions(PaperConfigurations::defaultOptions);
    }

    private static ConfigurationOptions defaultOptions(ConfigurationOptions options) {
        return options.serializers(builder -> builder
            .register(MapSerializer.TYPE, new MapSerializer(false))
            .register(new EnumValueSerializer())
            .register(new ComponentSerializer())
        );
    }

    @Override
    protected ObjectMapper.Factory.Builder createGlobalObjectMapperFactoryBuilder() {
        return defaultGlobalFactoryBuilder(super.createGlobalObjectMapperFactoryBuilder());
    }

    private static ObjectMapper.Factory.Builder defaultGlobalFactoryBuilder(ObjectMapper.Factory.Builder builder) {
        return builder.addDiscoverer(InnerClassFieldDiscoverer.globalConfig());
    }

    @Override
    protected YamlConfigurationLoader.Builder createGlobalLoaderBuilder() {
        return super.createGlobalLoaderBuilder()
            .defaultOptions(PaperConfigurations::defaultGlobalOptions);
    }

    private static ConfigurationOptions defaultGlobalOptions(ConfigurationOptions options) {
        return options
            .header(GLOBAL_HEADER)
            .serializers(builder -> builder.register(new PacketClassSerializer()));
    }

    @Override
    public GlobalConfiguration initializeGlobalConfiguration() throws ConfigurateException {
        GlobalConfiguration configuration = super.initializeGlobalConfiguration();
        GlobalConfiguration.set(configuration);
        return configuration;
    }

    @Override
    protected ContextMap.Builder createDefaultContextMap() {
        return super.createDefaultContextMap()
            .put(SPIGOT_WORLD_CONFIG_CONTEXT_KEY, SPIGOT_WORLD_DEFAULTS);
    }

    @Override
    protected ObjectMapper.Factory.Builder createWorldObjectMapperFactoryBuilder(final ContextMap contextMap) {
        return super.createWorldObjectMapperFactoryBuilder(contextMap)
            .addNodeResolver(new RequiresSpigotInitialization.Factory(contextMap.require(SPIGOT_WORLD_CONFIG_CONTEXT_KEY).get()))
            .addNodeResolver(new NestedSetting.Factory())
            .addDiscoverer(InnerClassFieldDiscoverer.worldConfig(contextMap));
    }

    @Override
    protected YamlConfigurationLoader.Builder createWorldConfigLoaderBuilder(final ContextMap contextMap) {
        return super.createWorldConfigLoaderBuilder(contextMap)
            .defaultOptions(options -> options
                .header(contextMap.require(WORLD_NAME).equals(WORLD_DEFAULTS) ? WORLD_DEFAULTS_HEADER : WORLD_HEADER.apply(contextMap))
                .serializers(serializers -> serializers
                    .register(new TypeToken<Reference2IntMap<?>>() {}, new FastutilMapSerializer.SomethingToPrimitive<Reference2IntMap<?>>(Reference2IntOpenHashMap::new, Integer.TYPE))
                    .register(new TypeToken<Reference2LongMap<?>>() {}, new FastutilMapSerializer.SomethingToPrimitive<Reference2LongMap<?>>(Reference2LongOpenHashMap::new, Long.TYPE))
                    .register(new TypeToken<Table<?, ?, ?>>() {}, new TableSerializer())
                    .register(new StringRepresentableSerializer())
                    .register(IntOrDefault.SERIALIZER)
                    .register(DoubleOrDefault.SERIALIZER)
                    .register(BooleanOrDefault.SERIALIZER)
                    .register(Duration.SERIALIZER)
                    .register(EngineMode.SERIALIZER)
                    .register(FallbackValueSerializer.create(contextMap.require(SPIGOT_WORLD_CONFIG_CONTEXT_KEY).get(), MinecraftServer::getServer))
                    .register(new RegistryValueSerializer<>(new TypeToken<EntityType<?>>() {}, Registry.ENTITY_TYPE_REGISTRY, true))
                    .register(new RegistryValueSerializer<>(Item.class, Registry.ITEM_REGISTRY, true))
                    .register(new RegistryHolderSerializer<>(new TypeToken<ConfiguredFeature<?, ?>>() {}, Registry.CONFIGURED_FEATURE_REGISTRY, false))
                    .register(new RegistryHolderSerializer<>(Item.class, Registry.ITEM_REGISTRY, true))
                )
            );
    }

    @Override
    protected void applyWorldConfigTransformations(final ContextMap contextMap, final ConfigurationNode node) throws ConfigurateException {
        final ConfigurationNode version = node.node(Configuration.VERSION_FIELD);
        final String world = contextMap.require(WORLD_NAME);
        if (version.virtual()) {
            LOGGER.warn("The world config file for " + world + " didn't have a version set, assuming latest");
            version.raw(WorldConfiguration.CURRENT_VERSION);
        }
        ConfigurationTransformation.Builder builder = ConfigurationTransformation.builder();
        for (NodePath path : RemovedConfigurations.REMOVED_WORLD_PATHS) {
            builder.addAction(path, TransformAction.remove());
        }
        builder.build().apply(node);
        // ADD FUTURE TRANSFORMS HERE
    }

    @Override
    protected void applyGlobalConfigTransformations(ConfigurationNode node) throws ConfigurateException {
        ConfigurationTransformation.Builder builder = ConfigurationTransformation.builder();
        for (NodePath path : RemovedConfigurations.REMOVED_GLOBAL_PATHS) {
            builder.addAction(path, TransformAction.remove());
        }
        builder.build().apply(node);
        // ADD FUTURE TRANSFORMS HERE
    }

    private static final List<Transformations.DefaultsAware> DEFAULT_AWARE_TRANSFORMATIONS = List.of(FeatureSeedsGeneration::apply);

    @Override
    protected void applyDefaultsAwareWorldConfigTransformations(final ContextMap contextMap, final ConfigurationNode worldNode, final ConfigurationNode defaultsNode) throws ConfigurateException {
        final ConfigurationTransformation.Builder builder = ConfigurationTransformation.builder();
        // ADD FUTURE TRANSFORMS HERE (these transforms run after the defaults have been merged into the node)
        DEFAULT_AWARE_TRANSFORMATIONS.forEach(transform -> transform.apply(builder, contextMap, defaultsNode));

        ConfigurationTransformation transformation;
        try {
            transformation = builder.build(); // build throws IAE if no actions were provided (bad zml)
        } catch (IllegalArgumentException ignored) {
            return;
        }
        transformation.apply(worldNode);
    }

    @Override
    public WorldConfiguration createWorldConfig(final ContextMap contextMap) {
        final String levelName = contextMap.require(WORLD_NAME);
        try {
            return super.createWorldConfig(contextMap);
        } catch (IOException exception) {
            throw new RuntimeException("Could not create world config for " + levelName, exception);
        }
    }

    @Override
    protected boolean isConfigType(final Type type) {
        return ConfigurationPart.class.isAssignableFrom(erase(type));
    }

    public void reloadConfigs(MinecraftServer server) {
        try {
            this.initializeGlobalConfiguration(reloader(this.globalConfigClass, GlobalConfiguration.get()));
            this.initializeWorldDefaultsConfiguration();
            for (ServerLevel level : server.getAllLevels()) {
                this.createWorldConfig(createWorldContextMap(level), reloader(this.worldConfigClass, level.paperConfig()));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not reload paper configuration files", ex);
        }
    }

    private static ContextMap createWorldContextMap(ServerLevel level) {
        return createWorldContextMap(level.convertable.levelDirectory.path(), level.serverLevelData.getLevelName(), level.dimension().location(), level.spigotConfig);
    }

    public static ContextMap createWorldContextMap(Path dir, String levelName, ResourceLocation worldKey, SpigotWorldConfig spigotConfig) {
        return ContextMap.builder()
            .put(WORLD_DIRECTORY, dir)
            .put(WORLD_NAME, levelName)
            .put(WORLD_KEY, worldKey)
            .put(SPIGOT_WORLD_CONFIG_CONTEXT_KEY, Suppliers.ofInstance(spigotConfig))
            .build();
    }

    public static PaperConfigurations setup(final Path legacyConfig, final Path configDir, final Path worldFolder, final File spigotConfig) throws Exception {
        if (needsConverting(legacyConfig)) {
            try {
                if (Files.exists(configDir) && !Files.isDirectory(configDir)) {
                    throw new RuntimeException("Paper needs to create a '" + CONFIG_DIR + "' folder in the root of your server. You already have a non-directory named '" + CONFIG_DIR + "'. Please remove it and restart the server.");
                }
                final Path backupDir = configDir.resolve(BACKUP_DIR);
                if (Files.exists(backupDir) && !Files.isDirectory(backupDir)) {
                    throw new RuntimeException("Paper needs to create a '" + BACKUP_DIR + "' directory in the '" + CONFIG_DIR + "' folder. You already have a non-directory named '" + BACKUP_DIR + "'. Please remove it and restart the server.");
                }
                createDirectoriesSymlinkAware(backupDir);
                final String backupFileName = legacyConfig.getFileName().toString() + ".old";
                final Path legacyConfigBackup = backupDir.resolve(backupFileName);
                if (Files.exists(legacyConfigBackup) && !Files.isRegularFile(legacyConfigBackup)) {
                    throw new RuntimeException("Paper needs to create a '" + backupFileName + "' file in the '" + BACKUP_DIR + "' folder. You already have a non-file named '" + backupFileName + "'. Please remove it and restart the server.");
                }
                Files.move(legacyConfig, legacyConfigBackup, StandardCopyOption.REPLACE_EXISTING); // make backup
                convert(legacyConfigBackup, configDir, worldFolder, spigotConfig);
            } catch (final IOException ex) {
                throw new RuntimeException("Could not convert '" + legacyConfig.getFileName().toString() + "' to the new configuration format", ex);
            }
        }
        try {
            createDirectoriesSymlinkAware(configDir);
            return new PaperConfigurations(configDir);
        } catch (final IOException ex) {
            throw new RuntimeException("Could not setup PaperConfigurations", ex);
        }
    }

    private static void convert(final Path legacyConfig, final Path configDir, final Path worldFolder, final File spigotConfig) throws Exception {
        createDirectoriesSymlinkAware(configDir);

        final YamlConfigurationLoader legacyLoader = ConfigurationLoaders.naturallySortedWithoutHeader(legacyConfig);
        final YamlConfigurationLoader globalLoader = ConfigurationLoaders.naturallySortedWithoutHeader(configDir.resolve(GLOBAL_CONFIG_FILE_NAME));
        final YamlConfigurationLoader worldDefaultsLoader = ConfigurationLoaders.naturallySortedWithoutHeader(configDir.resolve(WORLD_DEFAULTS_CONFIG_FILE_NAME));

        final ConfigurationNode legacy = legacyLoader.load();
        checkState(!legacy.virtual(), "can't be virtual");
        final int version = legacy.node(Configuration.LEGACY_CONFIG_VERSION_FIELD).getInt();

        final ConfigurationNode legacyWorldSettings = legacy.node("world-settings").copy();
        checkState(!legacyWorldSettings.virtual(), "can't be virtual");
        legacy.removeChild("world-settings");

        // Apply legacy transformations before settings flatten
        final YamlConfiguration spigotConfiguration = loadLegacyConfigFile(spigotConfig); // needs to change spigot config values in this transformation
        LegacyPaperConfig.transformation(spigotConfiguration).apply(legacy);
        spigotConfiguration.save(spigotConfig);
        legacy.mergeFrom(legacy.node("settings")); // flatten "settings" to root
        legacy.removeChild("settings");
        LegacyPaperConfig.toNewFormat().apply(legacy);
        globalLoader.save(legacy); // save converted node to new global location

        final ConfigurationNode worldDefaults = legacyWorldSettings.node("default").copy();
        checkState(!worldDefaults.virtual());
        worldDefaults.node(Configuration.LEGACY_CONFIG_VERSION_FIELD).raw(version);
        legacyWorldSettings.removeChild("default");
        LegacyPaperWorldConfig.transformation().apply(worldDefaults);
        LegacyPaperWorldConfig.toNewFormat().apply(worldDefaults);
        worldDefaultsLoader.save(worldDefaults);

        legacyWorldSettings.childrenMap().forEach((world, legacyWorldNode) -> {
            try {
                legacyWorldNode.node(Configuration.LEGACY_CONFIG_VERSION_FIELD).raw(version);
                LegacyPaperWorldConfig.transformation().apply(legacyWorldNode);
                LegacyPaperWorldConfig.toNewFormat().apply(legacyWorldNode);
                ConfigurationLoaders.naturallySortedWithoutHeader(worldFolder.resolve(world.toString()).resolve(WORLD_CONFIG_FILE_NAME)).save(legacyWorldNode); // save converted node to new location
            } catch (final ConfigurateException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static boolean needsConverting(final Path legacyConfig) {
        return Files.exists(legacyConfig) && Files.isRegularFile(legacyConfig);
    }

    @Deprecated
    public YamlConfiguration createLegacyObject(final MinecraftServer server) {
        YamlConfiguration global = YamlConfiguration.loadConfiguration(this.globalFolder.resolve(this.globalConfigFileName).toFile());
        ConfigurationSection worlds = global.createSection("__________WORLDS__________");
        worlds.set("__defaults__", YamlConfiguration.loadConfiguration(this.globalFolder.resolve(this.defaultWorldConfigFileName).toFile()));
        for (ServerLevel level : server.getAllLevels()) {
            worlds.set(level.getWorld().getName(), YamlConfiguration.loadConfiguration(getWorldConfigFile(level).toFile()));
        }
        return global;
    }

    @Deprecated
    public static YamlConfiguration loadLegacyConfigFile(File configFile) throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        if (configFile.exists()) {
            try {
                config.load(configFile);
            } catch (Exception ex) {
                throw new Exception("Failed to load configuration file: " + configFile.getName(), ex);
            }
        }
        return config;
    }

    @VisibleForTesting
    static ConfigurationNode createForTesting() {
        ObjectMapper.Factory factory = defaultGlobalFactoryBuilder(ObjectMapper.factoryBuilder()).build();
        ConfigurationOptions options = defaultGlobalOptions(defaultOptions(ConfigurationOptions.defaults()))
            .serializers(builder -> builder.register(type -> ConfigurationPart.class.isAssignableFrom(erase(type)), factory.asTypeSerializer()));
        return BasicConfigurationNode.root(options);
    }

    // Sym links are not correctly checked in createDirectories
    static void createDirectoriesSymlinkAware(Path path) throws IOException {
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path);
        }
    }
}
