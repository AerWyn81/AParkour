package me.davidml16.aparkour.utils;

import java.util.List;

public class WalkableBlocksUtil {

    public static boolean containsWalkable(List<XMaterial> walkableBlocks, XMaterial xMaterial) {
        return walkableBlocks.contains(xMaterial);
    }
}
