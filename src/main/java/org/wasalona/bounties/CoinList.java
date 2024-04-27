package org.wasalona.bounties;

import org.bukkit.Material;

public class CoinList {
    public static final Material copperCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_COPPER");
    public static final Material diamondCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_DIAMOND");
    public static final Material goldCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_GOLD");
    public static final Material netherCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_NETHERITE");
    public static final Material emeraldCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_EMERALD");
    public static final Material ironCoin = Material.valueOf("LIGHTMANSCURRENCY_COIN_IRON");

    public static final Material[] coinList = {copperCoin, diamondCoin, goldCoin, netherCoin, emeraldCoin, ironCoin};

    public static boolean contains(Material material) {
        for (Material coin : coinList) {
            if (coin == material) {
                return true;
            }
        }
        return false;
    }

    public static String getCommand(Material material) {
        for (int i = 0; i < coinList.length; i++) {
            if (coinList[i] == material) {
                switch (i) {
                    case 0:
                        return "lightmanscurrency:coin_copper";
                    case 1:
                        return "lightmanscurrency:coin_diamond";
                    case 2:
                        return "lightmanscurrency:coin_gold";
                    case 3:
                        return "lightmanscurrency:coin_netherite";
                    case 4:
                        return "lightmanscurrency:coin_emerald";
                    case 5:
                        return "lightmanscurrency:coin_iron";
                }
            }
        }
        return null; // Return null if material is not found in coinList
    }
}
