package org.bukkit.plugin;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Provides access to a Plugins description file, plugin.yaml
 */
public final class PluginDescriptionFile {
    private static final Yaml yaml = new Yaml(new SafeConstructor());
    private String name = null;
    private String main = null;
    private String classLoaderOf = null;
    private List<String> depend = null;
    private List<String> softDepend = null;
    private String version = null;
    private Map<String, Map<String, Object>> commands = null;
    private String description = null;
    private List<String> authors = null;
    private String website = null;
    private boolean database = false;
    private PluginLoadOrder order = PluginLoadOrder.POSTWORLD;
    private List<Permission> permissions = null;
    private PermissionDefault defaultPerm = PermissionDefault.OP;

    public PluginDescriptionFile(final InputStream stream) throws InvalidDescriptionException {
        loadMap((Map<?, ?>) yaml.load(stream));
    }

    /**
     * Loads a PluginDescriptionFile from the specified reader
     *
     * @param reader The reader
     * @throws InvalidDescriptionException If the PluginDescriptionFile is invalid
     */
    public PluginDescriptionFile(final Reader reader) throws InvalidDescriptionException {
        loadMap((Map<?, ?>) yaml.load(reader));
    }

    /**
     * Creates a new PluginDescriptionFile with the given detailed
     *
     * @param pluginName Name of this plugin
     * @param pluginVersion Version of this plugin
     * @param mainClass Full location of the main class of this plugin
     */
    public PluginDescriptionFile(final String pluginName, final String pluginVersion, final String mainClass) {
        name = pluginName;
        version = pluginVersion;
        main = mainClass;
    }

    /**
     * Saves this PluginDescriptionFile to the given writer
     *
     * @param writer Writer to output this file to
     */
    public void save(Writer writer) {
        yaml.dump(saveMap(), writer);
    }

    /**
     * Returns the name of a plugin
     *
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of a plugin
     *
     * @return String name
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the name of a plugin including the version
     *
     * @return String name
     */
    public String getFullName() {
        return name + " v" + version;
    }

    /**
     * Returns the main class for a plugin
     *
     * @return Java classpath
     */
    public String getMain() {
        return main;
    }

    public Map<String, Map<String, Object>> getCommands() {
        return commands;
    }

    public List<String> getDepend() {
        return depend;
    }

    public List<String> getSoftDepend() {
        return softDepend;
    }

    public PluginLoadOrder getLoad() {
        return order;
    }

    /**
     * Gets the description of this plugin
     *
     * @return Description of this plugin
     */
    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getWebsite() {
        return website;
    }

    public boolean isDatabaseEnabled() {
        return database;
    }

    public void setDatabaseEnabled(boolean database) {
        this.database = database;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public PermissionDefault getPermissionDefault() {
        return defaultPerm;
    }

    public String getClassLoaderOf() {
        return classLoaderOf;
    }

    private void loadMap(Map<?, ?> map) throws InvalidDescriptionException {
        try {
            name = map.get("name").toString();

            if (!name.matches("^[A-Za-z0-9 _.-]+$")) {
                throw new InvalidDescriptionException("name '" + name + "' contains invalid characters.");
            }
        } catch (NullPointerException ex) {
            throw new InvalidDescriptionException(ex, "name is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidDescriptionException(ex, "name is of wrong type");
        }

        try {
            version = map.get("version").toString();
        } catch (NullPointerException ex) {
            throw new InvalidDescriptionException(ex, "version is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidDescriptionException(ex, "version is of wrong type");
        }

        try {
            main = map.get("main").toString();
            if (main.startsWith("org.bukkit.")) {
                throw new InvalidDescriptionException("main may not be within the org.bukkit namespace");
            }
        } catch (NullPointerException ex) {
            throw new InvalidDescriptionException(ex, "main is not defined");
        } catch (ClassCastException ex) {
            throw new InvalidDescriptionException(ex, "main is of wrong type");
        }

        if (map.containsKey("commands")) {
            ImmutableMap.Builder<String, Map<String, Object>> commandsBuilder = ImmutableMap.<String, Map<String, Object>>builder();
            try {
                for (Map.Entry<?, ?> command : ((Map<?, ?>) map.get("commands")).entrySet()) {
                    ImmutableMap.Builder<String, Object> commandBuilder = ImmutableMap.<String, Object>builder();
                    for (Map.Entry<?, ?> commandEntry : ((Map<?, ?>) command.getValue()).entrySet()) {
                        if (commandEntry.getValue() instanceof Iterable) {
                            // This prevents internal alias list changes
                            ImmutableList.Builder<Object> commandSubList = ImmutableList.<Object>builder();
                            for (Object commandSubListItem : (Iterable<?>) commandEntry.getValue()) {
                                commandSubList.add(commandSubListItem);
                            }
                            commandBuilder.put(commandEntry.getKey().toString(), commandSubList.build());
                        } else {
                            commandBuilder.put(commandEntry.getKey().toString(), commandEntry.getValue());
                        }
                    }
                    commandsBuilder.put(command.getKey().toString(), commandBuilder.build());
                }
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "commands are of wrong type");
            }
            commands = commandsBuilder.build();
        }

        if (map.containsKey("class-loader-of")) {
            classLoaderOf = map.get("class-loader-of").toString();
        }

        if (map.containsKey("depend")) {
            ImmutableList.Builder<String> dependBuilder = ImmutableList.<String>builder();
            try {
                for (Object dependency : (Iterable<?>) map.get("depend")) {
                    dependBuilder.add(dependency.toString());
                }
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "depend is of wrong type");
            }
            depend = dependBuilder.build();
        }

        if (map.containsKey("softdepend")) {
            ImmutableList.Builder<String> softDependBuilder = ImmutableList.<String>builder();
            try {
                for (Object dependency : (Iterable<?>) map.get("softdepend")) {
                    softDependBuilder.add(dependency.toString());
                }
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "softdepend is of wrong type");
            }
            softDepend = softDependBuilder.build();
        }

        if (map.containsKey("database")) {
            try {
                database = (Boolean) map.get("database");
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "database is of wrong type");
            }
        }

        if (map.containsKey("website")) {
            website = map.get("website").toString();
        }

        if (map.containsKey("description")) {
            description = map.get("description").toString();
        }

        if (map.containsKey("load")) {
            try {
                order = PluginLoadOrder.valueOf(((String) map.get("load")).toUpperCase().replaceAll("\\W", ""));
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "load is of wrong type");
            } catch (IllegalArgumentException ex) {
                throw new InvalidDescriptionException(ex, "load is not a valid choice");
            }
        }

        if (map.containsKey("authors")) {
            ImmutableList.Builder<String> authorsBuilder = ImmutableList.<String>builder();
            if (map.containsKey("author")) {
                authorsBuilder.add(map.get("author").toString());
            }
            try {
                for (Object o : (Iterable<?>) map.get("authors")) {
                    authorsBuilder.add(o.toString());
                }
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "authors are of wrong type");
            }
            authors = authorsBuilder.build();
        } else if (map.containsKey("author")) {
            authors = ImmutableList.of(map.get("author").toString());
        } else {
            authors = ImmutableList.<String>of();
        }

        if (map.containsKey("default-permission")) {
            try {
                defaultPerm = PermissionDefault.getByName(map.get("default-permission").toString());
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "default-permission is of wrong type");
            } catch (IllegalArgumentException ex) {
                throw new InvalidDescriptionException(ex, "default-permission is not a valid choice");
            }
        }

        if (map.containsKey("permissions")) {
            try {
                Map<?, ?> perms = (Map<?, ?>) map.get("permissions");

                permissions = ImmutableList.copyOf(Permission.loadPermissions(perms, "Permission node '%s' in plugin description file for " + getFullName() + " is invalid", defaultPerm));
            } catch (ClassCastException ex) {
                throw new InvalidDescriptionException(ex, "permissions are of wrong type");
            }
        } else {
            permissions = ImmutableList.<Permission>of();
        }
    }

    private Map<String, Object> saveMap() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("name", name);
        map.put("main", main);
        map.put("version", version);
        map.put("database", database);
        map.put("order", order.toString());
        map.put("default-permission", defaultPerm.toString());

        if (commands != null) {
            map.put("command", commands);
        }
        if (depend != null) {
            map.put("depend", depend);
        }
        if (softDepend != null) {
            map.put("softdepend", softDepend);
        }
        if (website != null) {
            map.put("website", website);
        }
        if (description != null) {
            map.put("description", description);
        }

        if (authors.size() == 1) {
            map.put("author", authors.get(0));
        } else if (authors.size() > 1) {
            map.put("authors", authors);
        }

        if (classLoaderOf != null) {
            map.put("class-loader-of", classLoaderOf);
        }

        return map;
    }
}
