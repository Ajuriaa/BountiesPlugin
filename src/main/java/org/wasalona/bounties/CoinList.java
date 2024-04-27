package org.wasalona.bounties;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CoinList {
    public static final Material copperCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_COPPER");
    public static final Material diamondCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_DIAMOND");
    public static final Material goldCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_GOLD");
    public static final Material netherCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_NETHERITE");
    public static final Material emeraldCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_EMERALD");
    public static final Material ironCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_IRON");

    public static final Material[] coinList = {copperCoin, diamondCoin, goldCoin, netherCoin, emeraldCoin, ironCoin};

    public static boolean contains(ItemStack item) {
        for (Material coin : coinList) {
            if (item.getType() == coin) {
                return true;
            }
        }
        return false;
    }
}
