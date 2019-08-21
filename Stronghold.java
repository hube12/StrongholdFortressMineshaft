

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Stronghold {

    private static final Randy rand = new Randy();

    private static final int NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;

    private static final int START = 0;

    private static final int STRAIGHT = 1,
            PRISON = 2,
            LEFT_TURN = 3,
            RIGHT_TURN = 4,
            ROOM_CROSSING = 5,
            STAIRS_STRAIGHT = 6,
            STAIRS = 7,
            CROSSING = 8,
            CHEST_CORRIDOR = 9,
            LIBRARY = 10,
            PORTAL_ROOM = 11,
            CORRIDOR = 12;


    private static final int PIECES_COUNT = 12;

    private static final int[] WEIGHTS = {40, 5, 20, 20, 10, 5, 5, 5, 5, 10, 20, 0};
    private static final int[] MAXS = {0, 5, 0, 0, 6, 5, 5, 4, 4, 2, 1, 0};

    private static final Creator[] CREATORS = {
            null,
            Stronghold::createStraight,
            Stronghold::createPrison,
            Stronghold::createLeftTurn,
            Stronghold::createRightTurn,
            Stronghold::createRoomCrossing,
            Stronghold::createStairsStraight,
            Stronghold::createStairs,
            Stronghold::createCrosssing,
            Stronghold::createChestCorridor,
            Stronghold::createLibrary,
            Stronghold::createPortalRoom,
            Stronghold::createCorridor
    };
    private static final Extender[] EXTENDERS = {
            null,
            Stronghold::extendStraight,
            Stronghold::extendPrison,
            Stronghold::extendLeftTurn,
            Stronghold::extendRightTurn,
            Stronghold::extendRoomCrossing,
            Stronghold::extendStairsStraight,
            Stronghold::extendStairs,
            Stronghold::extendCrosssing,
            Stronghold::extendChestCorridor,
            null, //library
            Stronghold::extendPortalRoom,
            Stronghold::extendCorridor
    };
    private static final Runnable[] POST_CREATORS = {
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> {
            },
            () -> rand.nextInt(3),
            () -> rand.nextInt(3),
            () -> {
            },
            () -> {
            },
            () -> {
            }
    };

    @SuppressWarnings("unchecked")
    private static List<PieceInfo>[] placements = new List[PIECES_COUNT];

    static {
        Arrays.setAll(placements, i -> new ArrayList<>());
    }

    private static PieceInfo start;
    private static List<PieceInfo> pieceQueue = new ArrayList<>();
    private static int lastPlaced;

    private static void genStronghold(int chunkX, int chunkZ) {
        start = createStart((chunkX << 4) + 2, 64, (chunkZ << 4) + 2);
        placements[START].add(start);
        extendBridgeCrossing(start);
        while (!pieceQueue.isEmpty()) {
            int i = rand.nextInt(pieceQueue.size());

            PieceInfo piece = pieceQueue.remove(i);
            EXTENDERS[piece.type].extend(piece);
        }
    }


    // ===== CREATORS ===== //

    private static PieceInfo createStart(int x, int y, int z) {
        return new PieceInfo(START, 0, x, y, z, x + 19 - 1, 73, z + 19 - 1, rand.nextInt(4));
    }

    private static PieceInfo createStraight(int x, int y, int z, int depth, int facing) {
        PieceInfo pieceInfo = createRotated(STRAIGHT, depth, x, y, z, -1, -1, 0, 5, 5, 7, facing);
        pieceInfo.expandX(rand.nextInt(2) == 0);
        pieceInfo.expandZ(rand.nextInt(2) == 0);
        return pieceInfo;
    }

    private static PieceInfo createPrison(int x, int y, int z, int depth, int facing) {
        return createRotated(PRISON, depth, x, y, z, -1, -1, 0, 9, 5, 11, facing);
    }

    private static PieceInfo createLeftTurn(int x, int y, int z, int depth, int facing) {
        return createRotated(LEFT_TURN, depth, x, y, z, -1, -1, 0, 5, 5, 5, facing);
    }

    private static PieceInfo createRightTurn(int x, int y, int z, int depth, int facing) {
        return createRotated(RIGHT_TURN, depth, x, y, z, -1, -1, 0, 5, 5, 5, facing);
    }

    private static PieceInfo createRoomCrossing(int x, int y, int z, int depth, int facing) {
        return createRotated(ROOM_CROSSING, depth, x, y, z, -4, -1, 0, 11, 7, 11, facing);
    }

    private static PieceInfo createStairsStraight(int x, int y, int z, int depth, int facing) {
        return createRotated(STAIRS_STRAIGHT, depth, x, y, z, -1, -7, 0, 5, 11, 5, facing);
    }

    private static PieceInfo createStairs(int x, int y, int z, int depth, int facing) {
        return createRotated(STAIRS, depth, x, y, z, -1, -7, 0, 5, 11, 5, facing);
    }

    private static PieceInfo createCrosssing(int x, int y, int z, int depth, int facing) {
        PieceInfo pieceInfo = createRotated(CROSSING, depth, x, y, z, -4, -3, 0, 10, 9, 11, facing);
        pieceInfo.leftLow = rand.nextBoolean();
        pieceInfo.leftHigh = rand.nextBoolean();
        pieceInfo.rightLow = rand.nextBoolean();
        pieceInfo.rightHigh = rand.nextInt(3) > 0;
        return pieceInfo;
    }

    private static PieceInfo createChestCorridor(int x, int y, int z, int depth, int facing) {
        return createRotated(CHEST_CORRIDOR, depth, x, y, z, -1, -1, 0, 5, 5, 7, facing);
    }

    private static PieceInfo createLibrary(int x, int y, int z, int depth, int facing) {
        return createRotated(LIBRARY, depth, x, y, z, -4, -1, 0, 14, 11, 15, facing);
    }

    private static PieceInfo createPortalRoom(int x, int y, int z, int depth, int facing) {
        PieceInfo pieceInfo= createRotated(PORTAL_ROOM, depth, x, y, z, -4, -1, 0, 11, 8, 16, facing);
        if (!canStrongholdGoDeeper(pieceInfo) || intersectsAny(pieceInfo.xMin, pieceInfo.yMin, pieceInfo.zMin, pieceInfo.xMax, pieceInfo.yMax, pieceInfo.zMax))
        {
            pieceInfo = createRotated(PORTAL_ROOM, depth, x, y, z, -4, -1, 0, 14, 6, 15, facing);

            if (!canStrongholdGoDeeper(pieceInfo) || intersectsAny(pieceInfo.xMin, pieceInfo.yMin, pieceInfo.zMin, pieceInfo.xMax, pieceInfo.yMax, pieceInfo.zMax))
            {
                return null;
            }
        }
        return pieceInfo;

    }

    private static PieceInfo createCorridor(int x, int y, int z, int depth, int facing) {
        return createRotated(CORRIDOR, depth, x, y, z, -1, -1, 0, 5, 5, 7, facing);
    }

    private static PieceInfo createRotated(int type, int depth, int x, int y, int z, int relXMin, int relYMin, int relZMin, int relXMax, int relYMax, int relZMax, int facing) {
        int xMin, yMin, zMin, xMax, yMax, zMax;
        switch (facing) {
            case NORTH:
            case SOUTH:
                xMin = x + relXMin;
                xMax = x + relXMax - 1 + relXMin;
                break;
            case WEST:
                xMin = x - relZMax + 1 + relZMin;
                xMax = x + relZMin;
                break;
            case EAST:
                xMin = x + relZMin;
                xMax = x + relZMax - 1 + relZMin;
                break;
            default:
                throw new AssertionError();
        }
        yMin = y + relYMin;
        yMax = y + relYMax - 1 + relYMin;
        switch (facing) {
            case NORTH:
                zMin = z - relZMax + 1 + relZMin;
                zMax = z + relZMin;
                break;
            case SOUTH:
                zMin = z + relZMin;
                zMax = z + relZMax - 1 + relZMin;
                break;
            case WEST:
            case EAST:
                zMin = z + relXMin;
                zMax = z + relXMax - 1 + relXMin;
                break;
            default:
                throw new AssertionError();
        }
        return new PieceInfo(type, depth, xMin, yMin, zMin, xMax, yMax, zMax, facing);
    }


    // ===== EXTENDERS ===== //

    private static void extendStraight(PieceInfo pieceInfo) {
        extendForwards(pieceInfo, 1, 1, false);
        if (pieceInfo.expandX) {
            extendLeft(pieceInfo, 1, 2, false);
        }

        if (pieceInfo.expandZ) {
            extendRight(pieceInfo, 1, 2, false);
        }
    }

    private static void extendPrison(PieceInfo pieceInfo) {
        extendForwards(pieceInfo, 1, 1, false);

    }

    private static void extendLeftTurn(PieceInfo pieceInfo) {
        if (pieceInfo.facing != NORTH && pieceInfo.facing != EAST) {
            extendRight(pieceInfo, 1, 1, false);
        } else {
            extendLeft(pieceInfo, 1, 1, false);
        }
    }

    private static void extendRightTurn(PieceInfo pieceInfo) {
        if (pieceInfo.facing != NORTH && pieceInfo.facing != EAST) {
            extendLeft(pieceInfo, 1, 1, false);
        } else {
            extendRight(pieceInfo, 1, 1, false);
        }
    }

    private static void extendRoomCrossing(PieceInfo pieceInfo) {
        extendForwards(pieceInfo, 4, 1,false);
        extendLeft(pieceInfo, 1, 4,false);
        extendRight(pieceInfo ,1, 4,false);

    }

    private static void extendStairsStraight(PieceInfo pieceInfo) {
        extendForwards(pieceInfo, 1, 1, false);
    }

    private static void extendStairs(PieceInfo pieceInfo) {
        extendForwards(pieceInfo, 1, 1,false);

    }

    private static void extendCrosssing(PieceInfo pieceInfo) {
        int i = 3;
        int j = 5;
        if (pieceInfo.facing == WEST || pieceInfo.facing == NORTH) {
            i = 8 - i;
            j = 8 - j;
        }
        extendForwards(pieceInfo, 5, 1, false);
        if (pieceInfo.leftLow) {
            extendLeft(pieceInfo, i, 1, false);
        }

        if (pieceInfo.leftHigh) {
            extendLeft(pieceInfo, j, 7, false);
        }

        if (pieceInfo.rightLow) {
            extendRight(pieceInfo, i, 1, false);
        }

        if (pieceInfo.rightHigh) {
            extendRight(pieceInfo, j, 7, false);
        }
    }

    private static void extendChestCorridor(PieceInfo pieceInfo) {
        extendForwards(pieceInfo, 1, 1,false);
    }


    private static void extendPortalRoom(PieceInfo pieceInfo) {
        if (pieceInfo != null)
        {
            System.out.println("portal"); //((StructureStrongholdPieces.Stairs2)componentIn).strongholdPortalRoom = this;
        }
    }

    private static void extendCorridor(PieceInfo pieceInfo) {
        int horOffset;
        if (pieceInfo.facing == WEST || pieceInfo.facing == NORTH)
            horOffset = 5;
        else
            horOffset = 1;
        extendLeft(pieceInfo, horOffset, 0, rand.nextInt(8) > 0);
        extendRight(pieceInfo, horOffset, 0, rand.nextInt(8) > 0);
    }

    private static void extendForwards(PieceInfo pieceInfo, int horOffset, int vertOffset, boolean inCorridor) {
        switch (pieceInfo.facing) {
            case NORTH:
                extend(pieceInfo.xMin + horOffset, pieceInfo.yMin + vertOffset, pieceInfo.zMin - 1, pieceInfo.facing, pieceInfo.depth + 1, inCorridor);
                break;
            case SOUTH:
                extend(pieceInfo.xMin + horOffset, pieceInfo.yMin + vertOffset, pieceInfo.zMax + 1, pieceInfo.facing, pieceInfo.depth + 1, inCorridor);
                break;
            case WEST:
                extend(pieceInfo.xMin - 1, pieceInfo.yMin + vertOffset, pieceInfo.zMin + horOffset, pieceInfo.facing, pieceInfo.depth + 1, inCorridor);
                break;
            case EAST:
                extend(pieceInfo.xMax + 1, pieceInfo.yMin + vertOffset, pieceInfo.zMin + horOffset, pieceInfo.facing, pieceInfo.depth + 1, inCorridor);
                break;
        }
    }

    private static void extendLeft(PieceInfo pieceInfo, int horOffset, int vertOffset, boolean inCorridor) {
        switch (pieceInfo.facing) {
            case NORTH:
            case SOUTH:
                extend(pieceInfo.xMin - 1, pieceInfo.yMin + vertOffset, pieceInfo.zMin + horOffset, WEST, pieceInfo.depth + 1, inCorridor);
                break;
            case WEST:
            case EAST:
                extend(pieceInfo.xMin + horOffset, pieceInfo.yMin + vertOffset, pieceInfo.zMin - 1, NORTH, pieceInfo.depth + 1, inCorridor);
                break;
        }
    }

    private static void extendRight(PieceInfo pieceInfo, int horOffset, int vertOffset, boolean inCorridor) {
        switch (pieceInfo.facing) {
            case NORTH:
            case SOUTH:
                extend(pieceInfo.xMax + 1, pieceInfo.yMin + vertOffset, pieceInfo.zMin + horOffset, EAST, pieceInfo.depth + 1, inCorridor);
                break;
            case WEST:
            case EAST:
                extend(pieceInfo.xMin + horOffset, pieceInfo.yMin + vertOffset, pieceInfo.zMax + 1, SOUTH, pieceInfo.depth + 1, inCorridor);
                break;
        }
    }

    private static void extend(int x, int y, int z, int facing, int depth, boolean inCorridor) {
        if (Math.abs(x - start.xMin) <= 112 && Math.abs(z - start.zMin) <= 112) {
            int first;
            int pieceCount;
            int[] weights;
            int[] maxs;
            boolean[] allowConsecutives;
            if (inCorridor) {
                first = CORRIDOR_FIRST;
                pieceCount = CORRIDOR_PIECES_COUNT;
                weights = CORRIDOR_WEIGHTS;
                maxs = CORRIDOR_MAXS;
                allowConsecutives = CORRIDOR_ALLOW_CONSECUTIVE;
            } else {
                first = BRIDGE_FIRST;
                pieceCount = BRIDGE_PIECES_COUNT;
                weights = BRIDGE_WEIGHTS;
                maxs = BRIDGE_MAXS;
                allowConsecutives = BRIDGE_ALLOW_CONSECUTIVE;
            }

            boolean anyValid = false;
            int totalWeight = 0;
            for (int i = 0; i < pieceCount; i++) {
                if (maxs[i] > 0 && placements[first + i].size() >= maxs[i])
                    continue;
                if (maxs[i] > 0)
                    anyValid = true;
                totalWeight += weights[i];
            }
            if (anyValid && totalWeight > 0 && depth <= 30) {

                int tries = 0;
                while (tries < 5) {
                    tries++;
                    int n = rand.nextInt(totalWeight);
                    for (int i = 0; i < pieceCount; i++) {
                        if (maxs[i] > 0 && placements[first + i].size() >= maxs[i])
                            continue;
                        n -= weights[i];
                        if (n < 0) {
                            if (lastPlaced == first + i && !allowConsecutives[i]) {

                                break;


                            }

                            Creator creator = CREATORS[first + i];
                            System.out.println("Creating fortress piece " + (first + i) + " at (" + x + ", " + y + ", " + z + ") facing " + facing + " with depth " + depth + " queue size: " + pieceQueue.size() + " last placed: " + lastPlaced);
                            PieceInfo pieceInfo = creator.create(x, y, z, depth, facing);
                            if (!intersectsAny(pieceInfo.xMin, pieceInfo.yMin, pieceInfo.zMin, pieceInfo.xMax, pieceInfo.yMax, pieceInfo.zMax)) {
                                POST_CREATORS[first + i].run();
                                lastPlaced = first + i;

                                placements[first + i].add(pieceInfo);
                                pieceQueue.add(pieceInfo);
                                return;
                            }
                        }
                    }
                }

            }
        }
        PieceInfo pieceInfo = createEnd(x, y, z, depth, facing);
        if (!intersectsAny(pieceInfo.xMin, pieceInfo.yMin, pieceInfo.zMin, pieceInfo.xMax, pieceInfo.yMax, pieceInfo.zMax)) {
            rand.nextInt();
            placements[END].add(pieceInfo);
            pieceQueue.add(pieceInfo);
        }
    }
    private static boolean canStrongholdGoDeeper(PieceInfo pieceInfo){
        return pieceInfo!=null && pieceInfo.yMin>10;
    }
    private static boolean intersectsAny(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        for (List<PieceInfo> pieceInfoList : placements) {
            for (PieceInfo pieceInfo : pieceInfoList) {
                if (pieceInfo.xMin <= xMax && pieceInfo.xMax >= xMin && pieceInfo.zMin <= zMax && pieceInfo.zMax >= zMin && pieceInfo.yMin <= yMax && pieceInfo.yMax >= yMin)
                    return true;
            }
        }
        return false;
    }

    private static class PieceInfo {
        private final int type;
        private final int depth;
        private final int xMin, yMin, zMin;
        private final int xMax, yMax, zMax;
        private final int facing;
        private boolean expandX;
        private boolean expandZ;
        private boolean leftLow;
        private boolean leftHigh;
        private boolean rightLow;
        private boolean rightHigh;

        public PieceInfo(int type, int depth, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, int facing) {
            this.type = type;
            this.depth = depth;
            this.xMin = xMin;
            this.yMin = yMin;
            this.zMin = zMin;
            this.xMax = xMax;
            this.yMax = yMax;
            this.zMax = zMax;
            this.facing = facing;
        }

        public void expandX(boolean bool) {
            this.expandX = bool;
        }

        public void expandZ(boolean bool) {
            this.expandZ = bool;
        }

        public void leftLow(boolean bool) {
            this.leftLow = bool;
        }

        public void leftHigh(boolean bool) {
            this.leftHigh = bool;
        }

        public void rightLow(boolean bool) {
            this.rightLow = bool;
        }

        public void rightHigh(boolean bool) {
            this.rightHigh = bool;
        }
    }

    @FunctionalInterface
    private interface Creator {
        PieceInfo create(int x, int y, int z, int depth, int facing);
    }

    @FunctionalInterface
    private interface Extender {
        void extend(PieceInfo pieceInfo);
    }


    private static void setSeed(long worldSeed, int chunkX, int chunkZ) {
        rand.setSeed(worldSeed);
        long mulX = rand.nextLong();
        long mulZ = rand.nextLong();
        rand.setSeed((chunkX * mulX) ^ (chunkZ * mulZ) ^ worldSeed);
        rand.nextInt();
        System.out.println(rand.getSeed());
    }

    public static void main(String[] args) {
        setSeed(-695789727032293136L, 106, 3);

        genStronghold(106, 3);
    }

}