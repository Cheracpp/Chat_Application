package com.aymane.quickchat.dao;

import com.aymane.quickchat.model.ConversationSummary;
import com.aymane.quickchat.model.Message;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final HikariDataSource ds;
    private static final String DATABASE_URL = System.getenv("QUICKCHAT_URL");
    private static final String DATABASE_USER = System.getenv("QUICKCHAT_USER");
    private static final String DATABASE_PASSWORD = System.getenv("QUICKCHAT_PASSWORD");

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DATABASE_URL);
        config.setUsername(DATABASE_USER);
        config.setPassword(DATABASE_PASSWORD);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(config);
    }
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    // User Management
    public static boolean isUsernameUnique(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 0;
            }
        }
    }

    public static void addUser(String username, String plainPassword) throws SQLException {
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
        }
    }

    public static boolean isPasswordCorrect(String username, String plainPassword) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    String storedHashedPassword = resultSet.getString("password");
                    return BCrypt.checkpw(plainPassword, storedHashedPassword);
                }
                return false;
            }
        }
    }

    // Conversation Management


    // Creates a new conversation with the given title and participants. For direct messages, the title can be optional.
    public static int createConversationWithParticipants(String title, List<String> participantUsernames) throws SQLException {
        String insertConversationSql = "INSERT INTO conversations (title) VALUES (?)";
        String insertParticipantSql = "INSERT INTO conversation_participants (conversation_id, user_username) VALUES (?, ?)";
        int conversationId;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(insertConversationSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, title);
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating a new conversation failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        conversationId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating a new conversation succeeded, but failed to retrieve conversation ID.");
                    }
                }
            }

            try (PreparedStatement pstmtParticipant = conn.prepareStatement(insertParticipantSql)) {
                for (String username : participantUsernames) {
                    pstmtParticipant.setInt(1, conversationId);
                    pstmtParticipant.setString(2, username);
                    pstmtParticipant.addBatch(); // Add to batch
                }
                pstmtParticipant.executeBatch(); // Execute batched operations
                conn.commit();
            } catch (SQLException ex) {
                // Consider logging this exception
                throw ex;
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset autoCommit to true in finally block
                } catch (SQLException e) {
                    // Handle or log any exception on resetting autoCommit
                    e.printStackTrace(); // Consider using a logger instead
                }
            }
        }
        return conversationId;
    }
    public static int createConversationWithParticipants(List<String> participantUsernames) throws SQLException {
        return createConversationWithParticipants(null, participantUsernames); // Call the other method with null or "" for title
    }



    //Adds a new participant to an existing conversation.
    public static void addParticipantsToConversation(int conversationId, List<String> participantUsernames) throws SQLException {
        String insertParticipantSql = "INSERT INTO conversation_participants (conversation_id, user_username) VALUES (?,?)";
        Connection conn = null;
        try  {
            conn = getConnection();
            // Disable auto-commit to manually manage transactions
            conn.setAutoCommit(false);

            // PrepareStatement also managed via try-with-resources
            try (PreparedStatement pstmtParticipant = conn.prepareStatement(insertParticipantSql)) {
                for (String username : participantUsernames) {
                    pstmtParticipant.setInt(1, conversationId);
                    pstmtParticipant.setString(2, username);
                    pstmtParticipant.addBatch(); // Add to batch
                }

                pstmtParticipant.executeBatch(); // Execute batched operations
                conn.commit(); // Commit transaction if all operations succeed
            } catch (SQLException ex) {
                // Attempt to roll back the transaction in case of errors
                conn.rollback();
                // Rethrow exception to be handled or logged by the caller
                throw ex;
            }
        } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true); // Reset autoCommit to true in finally block
            } catch (SQLException e) {
                // Handle or log any exception on resetting autoCommit
                e.printStackTrace(); // Consider using a logger instead
            }
        }
    }
    }
    // Removes a participants from an existing conversation.
    public static void removeParticipantsFromConversation(int conversationId, List<String> participantUsernames) throws SQLException{
        String removeParticipantSql = "DELETE FROM conversation_participants WHERE conversation_id = ? AND user_username = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Disable auto-commit to manually manage transactions

            try (PreparedStatement pstmtParticipant = conn.prepareStatement(removeParticipantSql)) {
                for (String username : participantUsernames) {
                    pstmtParticipant.setInt(1, conversationId);
                    pstmtParticipant.setString(2, username);
                    pstmtParticipant.addBatch(); // Add to batch
                }

                pstmtParticipant.executeBatch(); // Execute batched operations
                conn.commit(); // Commit transaction if all operations succeed
            } catch (SQLException ex) {
                // Attempt to roll back the transaction in case of errors
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    // Handle or log rollback exception, if any
                    e.printStackTrace(); // Consider using a logger instead
                }
                // Rethrow exception to be handled or logged by the caller
                throw ex;
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset autoCommit to true in finally block
                } catch (SQLException e) {
                    // Handle or log any exception on resetting autoCommit
                    e.printStackTrace(); // Consider using a logger instead
                }
            }
        }
    }

    //Retrieves a list of participants in a conversation.
    public static List<String> getOtherConversationParticipants(int conversationId, String currentUsername) throws SQLException {
        String sql = "SELECT user_username FROM conversation_participants WHERE conversation_id = ? AND user_username <> ?";
        List<String> participantUsernames = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, conversationId);
            pstmt.setString(2, currentUsername);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
                    participantUsernames.add(resultSet.getString("user_username"));
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Failed at getting other conversation participants", ex);
        }

        return participantUsernames;
    }





    // Retrieves a list of conversation IDs that a user is part of, either as a sender or a recipient (for group conversations).
    public List<ConversationSummary> getUserConversations(String username) throws SQLException {
        List<ConversationSummary> conversations = new ArrayList<>();
        String sql = "SELECT c.conversation_id, c.title, MAX(m.sent_at) as last_message_time " +
                "FROM conversations c " +
                "JOIN conversation_participants cp ON c.conversation_id = cp.conversation_id " +
                "JOIN messages m ON c.conversation_id = m.conversation_id " +
                "WHERE cp.user_username = ? " +
                "GROUP BY c.conversation_id " +
                "ORDER BY last_message_time DESC";
        // Your SQL query as before;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int conversationId = rs.getInt("conversation_id");
                    String title = rs.getString("title");
                    Timestamp lastMessageTime = rs.getTimestamp("last_message_time");

                    // If title is null, assume it's a direct message and get the other participant's username
                    if (title == null) {
                        // Optimized method to get the other participant's username for direct messages
                        title = getDirectMessageOtherParticipant(conversationId, username);
                    }

                    conversations.add(new ConversationSummary(conversationId, title, lastMessageTime));
                }
            }
        }
        return conversations;
    }
    public static String getDirectMessageOtherParticipant(int conversationId, String username) throws SQLException {
        List<String> participants = getOtherConversationParticipants(conversationId, username);
        return participants.isEmpty() ? "" : participants.get(0); // Assuming at least one other participant exists
    }

    // Checks if a user is a participant in a specific conversation.
    public static void isUserInConversation(String username, String conversationId){}

    // Friend Management
    // Sends a friend request from one user to another.
    public static int sendFriendRequest(String username, String friendUsername) throws SQLException {
        String sendFriendRequestSql = "INSERT INTO friends (user_username, friend_username, status) VALUES (?, ?, ?)";
        int friendshipId = -1;

        Connection conn = null; // Declare outside to be accessible in finally block
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtCreateFriendship = conn.prepareStatement(sendFriendRequestSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtCreateFriendship.setString(1, username);
                pstmtCreateFriendship.setString(2, friendUsername);
                pstmtCreateFriendship.setString(3, "requested");
                pstmtCreateFriendship.executeUpdate();

                try (ResultSet resultSet = pstmtCreateFriendship.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        friendshipId = resultSet.getInt(1); // Correctly assuming 'friendship_id' is the first column
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                if (conn != null) {
                    try {
                        conn.rollback(); // Rollback within the same try to ensure it's only called if conn is not null
                    } catch (SQLException exRollback) {
                        // Log failure to rollback
                        exRollback.printStackTrace(); // Consider using a logger
                    }
                }
                throw new SQLException("Couldn't create the friendship", ex);
            }
        } catch (SQLException ex) {
            // Log or re-throw if necessary
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset autoCommit to true
                } catch (SQLException e) {
                    // Handle or log any exception on resetting autoCommit
                    e.printStackTrace(); // Consider using a logger
                }
                try {
                    conn.close(); // Ensure the connection is closed
                } catch (SQLException e) {
                    // Handle or log the closing connection exception
                    e.printStackTrace(); // Consider using a logger
                }
            }
        }

        return friendshipId;
    }

    // Accept Friend Request
    public static void acceptFriendRequest(String username, String friendUsername) throws SQLException {
        String updateFriendshipSql = "UPDATE friends SET status = 'accepted' WHERE user_username = ? AND friend_username = ? AND status = 'requested'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateFriendshipSql)) {
            pstmt.setString(1, friendUsername);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }
    // Rejects a friend request.
    public static void rejectFriendRequest(String username, String friendUsername) throws SQLException {
        // Assuming rejection involves deleting the request
        String deleteFriendRequestSql = "DELETE FROM friends WHERE user_username = ? AND friend_username = ? AND status = 'requested'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteFriendRequestSql)) {
            pstmt.setString(1, friendUsername);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    // Removes a friend relationship between two users.

    public static void removeFriend(String username, String friendUsername) throws SQLException {
        // This removes the friend relationship regardless of who initiated it
        String removeFriendSql = "DELETE FROM friends WHERE (user_username = ? AND friend_username = ?) OR (user_username = ? AND friend_username = ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(removeFriendSql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, friendUsername);
            pstmt.setString(3, friendUsername);
            pstmt.setString(4, username);
            pstmt.executeUpdate();
        }
    }
    // Checks if two users are friends.
    public static boolean isFriend(String username, String friendUsername) throws SQLException {
        String checkFriendSql = "SELECT COUNT(*) FROM friends WHERE ((user_username = ? AND friend_username = ?) OR (user_username = ? AND friend_username = ?)) AND status = 'accepted'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkFriendSql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, friendUsername);
            pstmt.setString(3, friendUsername);
            pstmt.setString(4, username);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0; // True if a friendship exists, false otherwise
                }
            }
        }
        return false; // Return false if no record is found
    }

    // Message Management
    // saving a message to the database
    public static int saveMessage(int conversationId, String senderUsername, String messageText) throws SQLException {
        // SQL query to insert a new message
        String insertMessageSql = "INSERT INTO messages (conversation_id, sender_username, message_text) VALUES (?, ?, ?)";

        int messageId = -1; // Initialize message ID to an invalid value

        try (Connection conn = getConnection(); // Try to get a connection
             PreparedStatement pstmt = conn.prepareStatement(insertMessageSql, Statement.RETURN_GENERATED_KEYS)) { // Prepare the statement

            pstmt.setInt(1, conversationId); // Set conversation ID
            pstmt.setString(2, senderUsername); // Set sender's username
            pstmt.setString(3, messageText); // Set message text

            int affectedRows = pstmt.executeUpdate(); // Execute the update

            // Check if the insert was successful
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) { // Get the generated keys
                    if (generatedKeys.next()) {
                        messageId = generatedKeys.getInt(1); // Retrieve the generated message ID
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SQLException("Error saving message to database", ex);
        }

        return messageId; // Return the message ID (or -1 if insertion was unsuccessful)
    }
    //Retrieves all messages within a conversation.
    public List<Message> getMessagesForConversation(int conversationId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.message_id, m.sender_username, m.message_text, m.sent_at FROM messages m WHERE m.conversation_id = ? ORDER BY m.sent_at ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, conversationId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setType("message");
                    message.setMessageId(rs.getInt("message_id"));
                    message.setSender(rs.getString("sender_username"));
                    message.setContent(rs.getString("message_text"));
                    message.setTimestamp(rs.getTimestamp("sent_at"));
                    messages.add(message);
                }
            }
        }
        return messages;
    }

    public static void deleteMessage(String messageId){} //Deletes a specific message from a conversation.







}
