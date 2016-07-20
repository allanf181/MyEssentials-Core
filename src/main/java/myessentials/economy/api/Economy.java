package myessentials.economy.api;

import br.com.gamemods.universalcoinsserver.UniversalCoinsServer;
import cpw.mods.fml.common.Loader;
import myessentials.MyEssentialsCore;
import myessentials.economy.universalcoins.UniversalCoinsEconomy;
import myessentials.economy.universalcoins.UniversalCoinsServerEconomy;
import myessentials.exception.EconomyException;
import myessentials.utils.ClassUtils;
import myessentials.utils.ItemUtils;
import myessentials.utils.PlayerUtils;
import myessentials.bukkit.BukkitCompat;
import myessentials.economy.forgeessentials.ForgeessentialsEconomy;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class Economy {
    public static final String CURRENCY_VAULT = "$Vault";
    public static final String CURRENCY_FORGE_ESSENTIALS = "$ForgeEssentials";
    public static final String CURRENCY_UNIVERSAL_COINS = "$UniversalCoins";
    public static final String CURRENCY_UNIVERSAL_COINS_SERVER = "$UniversalCoinsServer";

    private String costItemName;
    private Class<? extends IEconManager> econManagerClass;

    public Economy(String costItemName) {
        this.costItemName = costItemName;
        if(costItemName.equals(CURRENCY_VAULT)) {
            if (ClassUtils.isBukkitLoaded()) {
                econManagerClass = BukkitCompat.initEconomy();
            }
            if(econManagerClass == null)
                throw new EconomyException("Failed to initialize Vault economy!");
        } else if(costItemName.equals(CURRENCY_FORGE_ESSENTIALS)) {
            if(Loader.isModLoaded("ForgeEssentials"))
                econManagerClass = ForgeessentialsEconomy.class;
            if(econManagerClass == null)
                throw new EconomyException("Failed to initialize ForgeEssentials economy!");
        } else if(costItemName.equals(CURRENCY_UNIVERSAL_COINS)) {
            if(Loader.isModLoaded("universalcoins"))
                econManagerClass = UniversalCoinsEconomy.class;
            if(econManagerClass == null)
                throw new EconomyException("Failed to initialize UniversalCoins economy!");
        } else if(costItemName.equals(CURRENCY_UNIVERSAL_COINS_SERVER)) {
            if(Loader.isModLoaded("universalcoins"))
                econManagerClass = UniversalCoinsServerEconomy.class;
            if(econManagerClass == null)
                throw new EconomyException("Failed to initialize UniversalCoins economy!");
        }
    }

    public IEconManager economyManagerForUUID(UUID uuid) {
        if (econManagerClass == null) {
            return null;
        }
        try {
            IEconManager manager = econManagerClass.newInstance();
            manager.setPlayer(uuid);
            return manager;
        } catch(Exception ex) {
            MyEssentialsCore.instance.LOG.info("Failed to create IEconManager", ex);
        }

        return null; // Hopefully this doesn't break things...
    }

    /**
     * Takes the amount of money specified.
     * Returns false if player doesn't have the money necessary
     */
    public boolean takeMoneyFromPlayer(EntityPlayer player, int amount) {
        if(costItemName.equals(CURRENCY_FORGE_ESSENTIALS) || costItemName.equals(CURRENCY_VAULT) || costItemName.equals(CURRENCY_UNIVERSAL_COINS) || costItemName.equals(CURRENCY_UNIVERSAL_COINS_SERVER)) {
            IEconManager eco = economyManagerForUUID(player.getUniqueID());
            if (eco == null)
                return false;
            int wallet = eco.getWallet();
            if (wallet >= amount) {
                eco.removeFromWallet(amount);
                return true;
            }
            return false;
        } else {
            return PlayerUtils.takeItemFromPlayer(player, costItemName, amount);
        }
    }

    /**
     * Takes the amount of money specified.
     * Returns false if player doesn't have the money necessary
     */
    public void giveMoneyToPlayer(EntityPlayer player, int amount) {
        if (costItemName.equals(CURRENCY_FORGE_ESSENTIALS) || costItemName.equals(CURRENCY_VAULT) || costItemName.equals(CURRENCY_UNIVERSAL_COINS) || costItemName.equals(CURRENCY_UNIVERSAL_COINS_SERVER)) {
            IEconManager eco = economyManagerForUUID(player.getUniqueID());
            if (eco == null)
                return;
            eco.addToWallet(amount);
        } else {
            PlayerUtils.giveItemToPlayer(player, costItemName, amount);
        }
    }

    /**
     * Gets the currency string currently used.
     */
    public String getCurrency(int amount) {
        if(costItemName.equals(CURRENCY_FORGE_ESSENTIALS) || costItemName.equals(CURRENCY_VAULT) || costItemName.equals(CURRENCY_UNIVERSAL_COINS) || costItemName.equals(CURRENCY_UNIVERSAL_COINS_SERVER)) {
            if (econManagerClass == null) {
                return null;
            }
            try {
                IEconManager manager = econManagerClass.newInstance();
                return manager.currency(amount);
            } catch(Exception ex) {
                MyEssentialsCore.instance.LOG.info("Failed to create IEconManager", ex);
            }
            return "$";

        } else {
            return ItemUtils.itemStackFromName(costItemName).getDisplayName() + (amount == 1 ? "" : "s");
        }
    }
}