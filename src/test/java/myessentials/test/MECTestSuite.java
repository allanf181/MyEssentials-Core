package myessentials.test;

import metest.BaseSuite;
import myessentials.test.config.ConfigTest;
import myessentials.test.datasource.DatasourceTest;
import myessentials.test.economy.EconomyForgeEssentialsTest;
import myessentials.test.economy.EconomyItemTest;
import myessentials.test.entities.BlockPosTest;
import myessentials.test.entities.EntityPosTest;
import myessentials.test.entities.TreeTest;
import myessentials.test.entities.VolumeTest;
import myessentials.test.entities.sign.SignTest;
import myessentials.test.entities.tool.ToolTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

        EconomyItemTest.class,
        EconomyForgeEssentialsTest.class,
        DatasourceTest.class,
        ConfigTest.class,
        SignTest.class,
        ToolTest.class,
        BlockPosTest.class,
        ChunkPosTest.class,
        EntityPosTest.class,
        TreeTest.class,
        VolumeTest.class

})

public class MECTestSuite extends BaseSuite {
}