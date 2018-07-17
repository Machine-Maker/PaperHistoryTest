package io.papermc.paper;

import com.destroystokyo.paper.MaterialTags;
import io.papermc.paper.tag.EntityTags;
import org.bukkit.Bukkit;
import org.bukkit.TestServer;
import org.junit.Test;

import java.util.logging.Level;

public class EntityTagsTest {

    @Test
    public void testInitialize() {
        try {
            TestServer.getInstance();
            EntityTags.HORSES.getValues();
            assert true;
        } catch (Throwable e) {
            Bukkit.getLogger().log(Level.SEVERE, e.getMessage(), e);
            assert false;
        }
    }
}
