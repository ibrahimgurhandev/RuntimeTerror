package com.runtimeterror.model;

import com.runtimeterror.textparser.InputData;
import com.runtimeterror.textparser.Parser;

import java.io.*;
import java.util.*;

import static com.runtimeterror.utils.Pipe.apply;

class GameProcessor {
    private Database database;

    GameProcessor(boolean test) {
        // test constructor
    }

    GameProcessor() {
        newDatabase();
    }


    Map<String, Result<?>> start(Map<String, Result<?>> gameMap) {
        return
        apply(gameMap)
                .pipe(Parser::parseInput)
                .pipe(this::processHelp)
                .pipe(this::processStairs)
                .pipe(this::processUse)
                .pipe(this::processGet)
                .pipe(this::processGo)
                .pipe(this::processHide)
                .pipe(this::processMoveMonster)
                .pipe(this::processSaveGame)
                .pipe(this::processLoadGame)
                .pipe(this::processSkipPlayerTurn)
                .pipe(this::processMonsterEncounter)
                .pipe(this::processRoomChange)
                .result();
    }

    Map<String, Result<?>> processHelp(Map<String, Result<?>> gameMap) {
        boolean isProcessed = (boolean) gameMap.get("isProcessed").getResult();
        if (isProcessed) {
            return gameMap;
        }
        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String verb = inputData.getVerb();
        if ("HELP".equals(verb)) {
            gameMap.put("askedForHelp", new Result<>(true));
            gameMap.put("isProcessed", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("askedForHelp"));
        }
        return gameMap;
    }

    Map<String, Result<?>> processStairs(Map<String, Result<?>> gameMap) {
        boolean isProcessed = (boolean) gameMap.getOrDefault("isProcessed", new Result<>(false)).getResult();
        if (isProcessed) {
            return gameMap;
        }

        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String noun = inputData.getNoun();
        boolean hasStairs = (boolean) gameMap.get("hasStairs").getResult();
        String stairsRoomName = (String) gameMap.get("stairsRoom").getResult();
        @SuppressWarnings("unchecked")
        Map<String, Rooms> rooms = (Map<String, Rooms>) gameMap.get("rooms").getResult();
        Rooms stairsRoom = rooms.get(stairsRoomName);
        if (hasStairs && "stairs".equals(noun)) {
            gameMap.put("didUseStairs", new Result<>(true));
            gameMap.put("hidden", new Result<>(false));
            gameMap.put("isProcessed", new Result<>(true));
            gameMap.put("didChangeRoom", new Result<>(true));
            gameMap.put("playerCurrentRoom", new Result<>(stairsRoom));
            gameMap.put("shouldMonsterChangeRooms", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("didUseStairs"));
        } else if ("stairs".equals(noun)) {
            gameMap.put("triedToUseStairs", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("triedToUseStairs"));
        }
        return gameMap;
    }

    Map<String, Result<?>> processUse(Map<String, Result<?>> gameMap) {
        boolean isProcessed = (boolean) gameMap.get("isProcessed").getResult();
        if (isProcessed) {
            return gameMap;
        }
        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String verb = inputData.getVerb();
        if ("USE".equals(verb)) {
            gameMap.put("hidden", new Result<>(false));
            gameMap.put("isProcessed", new Result<>(true));
            gameMap.put("shouldMonsterChangeRooms", new Result<>(true));
            gameMap.put("triedToUseItem", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("triedToUseItem"));
            useItem(gameMap);
        }
        return gameMap;
    }

    Map<String, Result<?>> processGet(Map<String, Result<?>> gameMap) {
        boolean isProcessed = (boolean) gameMap.get("isProcessed").getResult();
        if (isProcessed) {
            return gameMap;
        }

        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String verb = inputData.getVerb();
        String noun = inputData.getNoun();
        Rooms currentRoom = (Rooms) gameMap.get("playerCurrentRoom").getResult();
        Item roomItem = currentRoom.getItem();
        if ("GET".equals(verb) && noun != null && roomItem != null && noun.equals(roomItem.getName())) {
            @SuppressWarnings("unchecked")
            List<Item> inventory = (List<Item>) gameMap.get("inventory").getResult();
            inventory.add(currentRoom.getItem());
            currentRoom.removeRoomItem();
            gameMap.put("didGetItem", new Result<>(true));
            gameMap.put("isProcessed", new Result<>(true));
            gameMap.put("hidden", new Result<>(false));
            gameMap.put("shouldMonsterChangeRooms", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("didGetItem"));
        } else if ("GET".equals(verb)) {
            gameMap.put("triedToGetItem", new Result<>(true));
            gameMap.put("isProcessed", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("triedToGetItem"));
        }
        return gameMap;
    }

    Map<String, Result<?>> processGo(Map<String, Result<?>> gameMap) {
        boolean isProcessed = (boolean) gameMap.get("isProcessed").getResult();
        if (isProcessed) {
            return gameMap;
        }
        @SuppressWarnings("unchecked")
        Map<String, Rooms> availableRooms = (HashMap<String, Rooms>) gameMap.get("availableRooms").getResult();
        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String noun = inputData.getNoun();
        String verb = inputData.getVerb();
        Rooms newRoom = availableRooms.get(noun);
        if ("GO".equals(verb) && newRoom != null) {
            gameMap.put("hidden", new Result<>(false));
            gameMap.put("isProcessed", new Result<>(true));
            gameMap.put("playerCurrentRoom", new Result<>(newRoom));
            gameMap.put("didChangeRoom", new Result<>(true));
            gameMap.put("shouldMonsterChangeRooms", new Result<>(true));
            gameMap.put("isValidDirection", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("didChangeRoom"));
        } else if("GO".equals(verb)) {
            gameMap.put("triedToGoDirection", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("triedToGoDirection"));
        }
        return gameMap;
    }

    Map<String, Result<?>> processHide(Map<String, Result<?>> gameMap) {
        boolean isProcessed = (boolean) gameMap.get("isProcessed").getResult();
        if (isProcessed) {
            return gameMap;
        }
        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String verb = inputData.getVerb();
        Rooms currentRoom = (Rooms) gameMap.get("playerCurrentRoom").getResult();
        if ("HIDE".equals(verb) && currentRoom.getHidingLocation() != null) {
            gameMap.put("hidden", new Result<>(true));
            gameMap.put("isProcessed", new Result<>(true));
            gameMap.put("shouldMonsterChangeRooms", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("didHide"));
        } else if ("HIDE".equals(verb)) {
            gameMap.put("triedToHide", new Result<>(true));
            gameMap.put("viewLabel", new Result<>("triedToHide"));
        }
        return gameMap;
    }

    Map<String, Result<?>> processMoveMonster(Map<String, Result<?>> gameMap) {
        boolean shouldMonsterChangeRooms = (boolean) gameMap.get("shouldMonsterChangeRooms").getResult();
        if (shouldMonsterChangeRooms) {
            Random r = new Random();
            @SuppressWarnings("unchecked")
            Map<String, Rooms> allRooms = (HashMap<String, Rooms>) gameMap.get("rooms").getResult();
            List<Rooms> allRoomsList = new ArrayList<>(allRooms.values());
            int randomInt = r.nextInt(allRoomsList.size() - 1) + 1;
            Rooms monsterNewRoom = allRoomsList.get(randomInt);
            gameMap.put("monsterCurrentRoom", new Result<>(monsterNewRoom));
            gameMap.put("didMonsterMove", new Result<>(true));
        }
        return gameMap;
    }

    Map<String, Result<?>> processSaveGame(Map<String, Result<?>> gameMap) {
        boolean isProcessed = (boolean) gameMap.get("isProcessed").getResult();
        if (isProcessed) {
            return gameMap;
        }

        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String verb = inputData.getVerb();

        if("SAVE".equals(verb)){
            HashMap<String, Object> gameObjects = new HashMap<>();
            gameObjects.put("gameMap", gameMap);
            try {
                FileOutputStream fos = new FileOutputStream("Game/gameData/savedGameData.txt");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(gameObjects);
                oos.flush();
                oos.close();
                fos.close();
                gameMap.put("viewLabel", new Result<>("didSaveGame"));
            } catch (FileNotFoundException e) {
                gameMap.put("viewLabel", new Result<>("triedToSaveGame"));
                System.out.println("Failed to load the game files:");
                System.out.println(e.getMessage());
            } catch (IOException e) {
                gameMap.put("viewLabel", new Result<>("triedToSaveGame"));
                e.printStackTrace();
            }
            gameMap.put("isProcessed", new Result<>(true));
        }

        return gameMap;
    }

    @SuppressWarnings("unchecked")
    Map<String, Result<?>> processLoadGame(Map<String, Result<?>> gameMap) {
        boolean isProcessed = (boolean) gameMap.get("isProcessed").getResult();
        if (isProcessed) {
            return gameMap;
        }

        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String verb = inputData.getVerb();
        boolean gameLoaded = (boolean) gameMap.get("gameLoaded").getResult();

        if("LOAD".equals(verb) && !gameLoaded) {
            try {
                FileInputStream fis = new FileInputStream("Game/gameData/savedGameData.txt");
                ObjectInputStream ois = new ObjectInputStream(fis);
                @SuppressWarnings("unchecked")
                HashMap<String, Object> data = (HashMap<String, Object>) ois.readObject();
                fis.close();
                gameMap = (Map<String, Result<?>>) data.get("gameMap");
                System.out.println(gameMap);
                gameMap.put("gameLoaded", new Result<>(true));
                gameMap.put("viewLabel", new Result<>("didLoadGame"));
            } catch (Exception e) {
                return gameMap;
            }
        } else if("LOAD".equals(verb)) {
            gameMap.put("viewLabel", new Result<>("triedToLoadGame"));
        }
        return gameMap;
    }

    Map<String, Result<?>> processSkipPlayerTurn(Map<String, Result<?>> gameMap) {
        return gameMap;
    }

    Map<String, Result<?>> processMonsterEncounter(Map<String, Result<?>> gameMap) {
        boolean didMonsterMove = (boolean) gameMap.get("didMonsterMove").getResult();
        if (didMonsterMove) {
            Rooms playerCurrentRoom = (Rooms) gameMap.get("playerCurrentRoom").getResult();
            Rooms monsterCurrentRoom = (Rooms) gameMap.get("monsterCurrentRoom").getResult();
            if (playerCurrentRoom == monsterCurrentRoom) {
                gameMap.put("isGameOver", new Result<>(true));
                gameMap.put("isKilledByMonster", new Result<>(true));
            }
        }
        return gameMap;
    }

    Map<String, Result<?>> processRoomChange(Map<String, Result<?>> gameMap) {
        gameMap.put("isProcessed", new Result<>(false));
        boolean didChangeRoom = (boolean) gameMap.get("didChangeRoom").getResult();
        if (didChangeRoom) {
            Rooms newRoom = (Rooms) gameMap.get("playerCurrentRoom").getResult();
            gameMap.put("hasStairs", new Result<>(newRoom.hasStairs()));
            gameMap.put("stairsRoom", new Result<>(newRoom.getStairsNeighborName()));
            gameMap.put(("availableRooms"), new Result<>(newRoom.getRoomNeighbors()));
        }
        return gameMap;
    }

    Map<String, Result<?>> getGameMap() {
        return database.getDataAsHashMap();
    }

    void newDatabase() {
        database = new Database();
    }

    @SuppressWarnings("unchecked")
    void useItem(Map<String, Result<?>> gameMap) {
        InputData inputData = (InputData) gameMap.get("inputData").getResult();
        String noun = inputData.getNoun();
        List<Item> inventory = (List<Item>) gameMap.get("inventory").getResult();
        Item itemToRemove = new Item();
        for (Item item : inventory) {
            if (item != null && noun.equals(item.getName())) {
                gameMap.put("triedToUseItem", new Result<>(false));
                gameMap.put("usedItem", new Result<>(true));
                gameMap.put("itemUsed", new Result<>(noun));
                gameMap.put("viewLabel", new Result<>("usedItem"));
                itemToRemove = item;
                break;
            }
        }
        inventory.remove(itemToRemove);
    }

}