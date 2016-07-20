package myessentials.economy.universalcoins;

import myessentials.economy.IEconManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import universalcoins.commands.UCBalance;
import universalcoins.commands.UCGive;
import universalcoins.commands.UCSend;
import universalcoins.proxy.CommonProxy;
import universalcoins.util.UniversalAccounts;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UniversalCoinsEconomy implements IEconManager
{
    private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
    private static final Item[] coins = new Item[] {CommonProxy.itemCoin,
            CommonProxy.itemSmallCoinStack, CommonProxy.itemLargeCoinStack,
            CommonProxy.itemSmallCoinBag, CommonProxy.itemLargeCoinBag};

    private UUID playerId;

    @Override
    public void setPlayer(UUID uuid)
    {
        playerId = uuid;
    }

    private EntityPlayer getOnlinePlayer()
    {
        //noinspection unchecked
        List<EntityPlayer> players = (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for(EntityPlayer player: players)
            if(player.getPersistentID().equals(playerId))
                return player;

        return null;
    }

    private int[] getValidCards(EntityPlayer player)
    {
        String playerId = player.getPersistentID().toString();

        int[] accounts = new int[]{-1,-1};

        for (int i = 0; i < player.inventory.mainInventory.length; ++i)
        {
            ItemStack stack = player.inventory.mainInventory[i];
            if(stack == null || stack.stackTagCompound == null) continue;
            Item item = stack.getItem();
            if (item == CommonProxy.itemUCCard || item == CommonProxy.itemEnderCard)
            {
                String account;
                if(stack.stackTagCompound.getString("Owner").equals(playerId)
                        && !(account=stack.stackTagCompound.getString("Account")).isEmpty())
                {
                    String playerAccount = UniversalAccounts.getInstance().getPlayerAccount(playerId);
                    if(account.equals(playerAccount))
                    {
                        if (accounts[0] > -1)
                            continue;

                        accounts[0] = i;

                        if(accounts[1] > -1)
                            return accounts;
                        continue;
                    }

                    playerAccount = UniversalAccounts.getInstance().getCustomAccount(playerId);
                    if(account.equals(playerAccount))
                    {
                        if(accounts[1] > -1)
                            continue;

                        accounts[1] = i;

                        if(accounts[0] > -1)
                            return accounts;
                    }
                }
            }
        }

        return accounts;
    }

    @Override
    public void addToWallet(int amountToAdd)
    {
        /*
        EntityPlayer player = getOnlinePlayer();
        int leftOvers;
        if(player != null)
        {
            int[] cardSlots = getValidCards(player);
            int cardSlot = cardSlots[0] > -1? cardSlots[0] : cardSlots[1];
            if(cardSlot > -1)
            {
                String account = player.inventory.mainInventory[cardSlot].stackTagCompound.getString("Account");
                UniversalAccounts.getInstance().creditAccount(account, amountToAdd);
                System.out.println(amountToAdd+" credited to the "+player.getDisplayName()+"'s account number "+account);
                return;
            }

            try
            {
                leftOvers = givePlayerCoins(player, amountToAdd);
                player.inventoryContainer.detectAndSendChanges();
                System.out.println(amountToAdd+" added to the "+player.getDisplayName()+"'s inventory. Left overs: "+leftOvers);
            } catch (Exception e)
            {
                e.printStackTrace();
                leftOvers = amountToAdd;
            }

            if(addToAnyAccount(playerId, leftOvers))
                return;

            dropToThePlayer(player, leftOvers);
            return;
        }
        else
            leftOvers = amountToAdd;

        if(addToAnyAccount(playerId, leftOvers))
            return;

        String account = UniversalAccounts.getInstance().getOrCreatePlayerAccount(playerId.toString());
        UniversalAccounts.getInstance().creditAccount(account, leftOvers);
        */

        if(addToAnyAccount(playerId, amountToAdd))
            return;

        EntityPlayer player = getOnlinePlayer();
        if(player != null)
        {
            int leftOvers = givePlayerCoins(player, amountToAdd);
            System.out.println((amountToAdd-leftOvers)+" added to the "+player.getDisplayName()+"'s inventory. Left overs: "+leftOvers);
            if(leftOvers > 0)
            {
                dropToThePlayer(player, leftOvers);
                System.out.println("Dropped "+leftOvers+" to the "+player.getDisplayName()+"'s location");
            }
            return;
        }

        String account = UniversalAccounts.getInstance().getOrCreatePlayerAccount(playerId.toString());
        UniversalAccounts.getInstance().creditAccount(account, amountToAdd);
        System.out.println("Credited "+amountToAdd+" to a recently created "+playerId+"'s account with number "+account);
    }

    private void dropToThePlayer(EntityPlayer player, int amountToDrop)
    {
        System.out.println(amountToDrop+" dropped to the player "+player.getDisplayName());

        while (amountToDrop > 0) {
            int logVal = Math.min((int) (Math.log(amountToDrop) / Math.log(9)), 4);
            int stackSize = Math.min((int) (amountToDrop / Math.pow(9, logVal)), 64);
            ItemStack test = new ItemStack(coins[logVal], stackSize);
            player.entityDropItem(test, 0.0F);
            amountToDrop -= Math.pow(9, logVal) * stackSize;
        }
    }

    private boolean addToAnyAccount(UUID playerId, int amountToAdd)
    {
        String id = playerId.toString();
        String playerAccount = UniversalAccounts.getInstance().getPlayerAccount(id);
        if(playerAccount.isEmpty())
        {
            playerAccount = UniversalAccounts.getInstance().getCustomAccount(id);
            if(playerAccount.isEmpty())
                return false;
        }

        UniversalAccounts.getInstance().creditAccount(playerAccount, amountToAdd);
        System.out.println(amountToAdd+" credited to the "+playerId+"'s account number "+playerAccount);
        return true;
    }

    @Override
    public int getWallet()
    {
        EntityPlayer player = getOnlinePlayer();
        if(player != null)
        {
            int amount = 0;
            int[] cardSlots = getValidCards(player);
            for(int cardSlot: cardSlots)
            {
                if(cardSlot == -1) continue;

                String account = player.inventory.mainInventory[cardSlot].stackTagCompound.getString("Account");
                amount += UniversalAccounts.getInstance().getAccountBalance(account);
            }

            try
            {
                amount += getPlayerCoins(player);
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            return amount;
        }

        String playerAccount = UniversalAccounts.getInstance().getPlayerAccount(playerId.toString());
        if(playerAccount.isEmpty())
            return 0;

        return UniversalAccounts.getInstance().getAccountBalance(playerAccount);
    }

    @Override
    public boolean removeFromWallet(int amountToSubtract)
    {
        if (amountToSubtract == 0)
            return true;

        if (amountToSubtract < 0)
            return false;

        int available = getWallet();

        if (available < amountToSubtract)
            return false;

        EntityPlayer player = getOnlinePlayer();
        if (player != null)
        {
            int[] cardSlots = getValidCards(player);
            int subtracted = 0;
            for (int cardSlot : cardSlots)
            {
                if (cardSlot == -1) continue;

                String account = player.inventory.mainInventory[cardSlot].stackTagCompound.getString("Account");
                int balance = UniversalAccounts.getInstance().getAccountBalance(account);
                if (balance >= amountToSubtract)
                {
                    if (UniversalAccounts.getInstance().debitAccount(account, amountToSubtract))
                    {
                        System.out.println(amountToSubtract + " debited from the " + player.getDisplayName() + "'s account number " + account);
                        return true;
                    }
                } else if (balance > 0)
                {
                    if (UniversalAccounts.getInstance().debitAccount(account, balance))
                    {
                        System.out.println(balance + " debited from the " + player.getDisplayName() + "'s account number " + account);
                        amountToSubtract -= balance;
                        subtracted += balance;
                    }
                }
            }

            try
            {
                int taken = takePlayerCoins(player, amountToSubtract);
                subtracted += taken;
                if (taken > amountToSubtract)
                {
                    int change = taken - amountToSubtract;
                    int leftOver = givePlayerCoins(player, change);
                    if(leftOver > 0)
                        dropToThePlayer(player, leftOver);
                    subtracted -= change;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                addToAnyAccount(playerId, subtracted);
                player.inventoryContainer.detectAndSendChanges();
                return false;
            }

            if (subtracted != amountToSubtract)
            {
                addToAnyAccount(playerId, subtracted);
                player.inventoryContainer.detectAndSendChanges();
                return false;
            }

            player.inventoryContainer.detectAndSendChanges();
            return true;
        }

        String playerAccount = UniversalAccounts.getInstance().getPlayerAccount(playerId.toString());
        return !playerAccount.isEmpty() && UniversalAccounts.getInstance().debitAccount(playerAccount, amountToSubtract);

    }

    @Override
    public void setWallet(int setAmount, EntityPlayer player)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String currency(int amount)
    {
        ItemStack stack = new ItemStack(CommonProxy.itemCoin);
        return NumberFormat.getIntegerInstance().format(amount)+" "+stack.getDisplayName();
    }

    @Override
    public String getMoneyString()
    {
        return currency(getWallet());
    }

    @Override
    public void save()
    {
    }

    @Override
    public Map<String, Integer> getItemTables()
    {
        return null;
    }

    public static int givePlayerCoins(EntityPlayer recipient, int coinsLeft) {
        while (coinsLeft > 0) {
            // use logarithm to find largest cointype for coins being sent
            int logVal = Math.min((int) (Math.log(coinsLeft) / Math.log(9)), 4);
            int stackSize = Math.min((int) (coinsLeft / Math.pow(9, logVal)), 64);
            // add a stack to the recipients inventory
            if (recipient.inventory.getFirstEmptyStack() != -1) {
                recipient.inventory.addItemStackToInventory(new ItemStack(coins[logVal], stackSize));
                coinsLeft -= (stackSize * Math.pow(9, logVal));
            } else {
                for (int i = 0; i < recipient.inventory.getSizeInventory(); i++) {
                    ItemStack stack = recipient.inventory.getStackInSlot(i);
                    for (int j = 0; j < coins.length; j++) {
                        if (stack != null && stack.getItem() == coins[j]) {
                            int amountToAdd = (int) Math.min(coinsLeft / Math.pow(9, j),
                                    stack.getMaxStackSize() - stack.stackSize);
                            stack.stackSize += amountToAdd;
                            recipient.inventory.setInventorySlotContents(i, stack);
                            coinsLeft -= (amountToAdd * Math.pow(9, j));
                        }
                    }
                }
                return coinsLeft; // return change
            }
        }
        return 0;
    }

    public static int getPlayerCoins(EntityPlayer player) {
        int coinsFound = 0;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            for (int j = 0; j < coins.length; j++) {
                if (stack != null && stack.getItem() == coins[j]) {
                    coinsFound += stack.stackSize * multiplier[j];
                }
            }
        }
        return coinsFound;
    }

    public static int takePlayerCoins(EntityPlayer player, int requestedSendAmount) {
        int coinsFound = 0;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            for (int j = 0; j < coins.length; j++) {
                if (stack != null && stack.getItem() == coins[j] && coinsFound < requestedSendAmount) {
                    coinsFound += stack.stackSize * multiplier[j];
                    player.inventory.setInventorySlotContents(i, null);
                }
            }
        }
        return coinsFound;
    }
}
