package de.freesoccerhdx.permlegend;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SQLDatabase {

    private final String JDBC_URL;

    private HashMap<UUID, PlayerPermissionData> playerPermissionData = new HashMap<>();

    public SQLDatabase(Plugin plugin) {
        JDBC_URL = "jdbc:sqlite:"+new File(plugin.getDataFolder(), "/data.db").getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        } 

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

            @Override
            public void run() {
                checkIfTablesExists();
                loadTables();
            }

        });
    }

    public PlayerPermissionData getPlayerPermissionData(UUID uuid) {
        return this.playerPermissionData.get(uuid);
    }
    
    private void checkIfTablesExists() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            createPlayerTable(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTables() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            loadPlayerData(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createPlayerTable(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS SpielerTabelle ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "UUID VARCHAR(36) NOT NULL,"
                + "Name VARCHAR(255) NOT NULL,"
                + "GroupName VARCHAR(50),"
                + "TempGroupName VARCHAR(50),"
                + "TempGroupEnd LONG"
                + ")";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
            preparedStatement.executeUpdate();
        }
    }

    
    private void loadPlayerData(Connection connection) throws SQLException {
        String query = "SELECT ID, UUID, Name, GroupName, TempGroupName, TempGroupEnd FROM SpielerTabelle";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int primaryID = resultSet.getInt("ID");
                String uuids = resultSet.getString("UUID");
                String name = resultSet.getString("Name");
                String groupName = resultSet.getString("GroupName");
                String tempGroupName = resultSet.getString("TempGroupName");
                long tempGroupEnd = resultSet.getLong("TempGroupEnd");

                UUID uuid = UUID.fromString(uuids);

                System.out.println(primaryID + ", " + uuid + ", " + name + ", " + groupName + ", " + tempGroupName + ", " + tempGroupEnd);
                playerPermissionData.put(uuid, new PlayerPermissionData(uuid, name, groupName, tempGroupName, tempGroupEnd));
            }
        }
    }

    private boolean doesPlayerDataExist(Connection connection, UUID uuid) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM SpielerTabelle WHERE UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, uuid.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        }
        return false;
    }

    public void savePlayerData(PlayerPermissionData playerData) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            if(!doesPlayerDataExist(connection, playerData.getUuid())) {
                String query = "INSERT INTO SpielerTabelle (UUID, Name, GroupName, TempGroupName, TempGroupEnd) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, playerData.getUuid().toString());
                    preparedStatement.setString(2, playerData.getPlayerName());
                    preparedStatement.setString(3, playerData.getGroupName());
                    preparedStatement.setString(4, playerData.getTempGroupName());
                    preparedStatement.setLong(5, playerData.getTempGroupEnd());

                    preparedStatement.executeUpdate();
                }
            } else {        
                updatePlayerData(connection, playerData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean updatePlayerData(Connection connection, PlayerPermissionData playerData) throws SQLException {
        String query = "UPDATE SpielerTabelle SET Name = ?, GroupName = ?, TempGroupName = ?, TempGroupEnd = ? WHERE UUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, playerData.getPlayerName());
            preparedStatement.setString(2, playerData.getGroupName());
            preparedStatement.setString(3, playerData.getTempGroupName());
            preparedStatement.setLong(4, playerData.getTempGroupEnd());
            preparedStatement.setString(5, playerData.getUuid().toString());

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public void updatePlayerData(PlayerPermissionData playerData) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL)) {
            updatePlayerData(connection, playerData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}