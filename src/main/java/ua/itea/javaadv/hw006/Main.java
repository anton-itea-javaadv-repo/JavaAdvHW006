package ua.itea.javaadv.hw006;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static final String SQL_SELECT_ALL = "select id, name, age from aSimpleTable;";
    public static final String SQL_SELECT_ONE = "select id, name, age from aSimpleTable where id = ?;";
    public static final String SQL_INSERT_ONE = "insert aSimpleTable(name, age) values (?, ?);";

    public static void main(String[] args) {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        Connection conn = null;

        try {
            URL url = new File("connection_props.ini").toURI().toURL();
            Properties props = new Properties();
            props.load(url.openStream());

            String host = props.getProperty("host");
            String port = props.getProperty("port");
            String db = props.getProperty("db");
            String user = props.getProperty("user");
            String password = props.getProperty("password");

            if (StringUtils.isEmpty(host)) {
                System.out.println("Empty host!");
                return;
            }

            if (StringUtils.isEmpty(port)) {
                System.out.println("Empty port!");
                return;
            }

            if (StringUtils.isEmpty(db)) {
                System.out.println("Empty db!");
                return;
            }

            if (StringUtils.isEmpty(user)) {
                System.out.println("Empty user!");
                return;
            }

//            if (StringUtils.isEmpty(password)) {
//                System.out.println("Empty password!");
//            }

            if (!NumberUtils.isParsable(port)) {
                System.out.println("Port is not a number!");
            }

            String dbUrl =
                    String.format(
                            "jdbc:mysql://%s:%s/%s?user=%s&password=%s",
                            host,
                            port,
                            db,
                            user,
                            password);

            conn = DriverManager.getConnection(dbUrl);

            System.out.println("Connected successfully!");
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (conn == null) {
            return;
        }

        Scanner commands = new Scanner(System.in);

        try {
            while (true) {
                System.out.print("command: > ");
                String command = commands.nextLine();

                if (command != null && !command.trim().isEmpty()) {
                    List<String> splitCommand = Arrays.stream(command.split(" "))
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(it -> !it.isEmpty())
                            .collect(Collectors.toList());
                    if ((command.toLowerCase().startsWith("list")
                            && (splitCommand.size() == 1
                            && splitCommand.get(0).equalsIgnoreCase("list")))
                    ) {
                        PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            System.out.print(rs.getString(1));
                            System.out.print(", ");
                            System.out.print(rs.getString(2));
                            System.out.print(", ");
                            System.out.println(rs.getString(3));
                        }
                        rs.close();
                        ps.close();
                    } else if (command.toLowerCase().startsWith("put ")) {
                        if (splitCommand.size() != 3) {
                            System.out.println("Invalid command...");
                        } else {
                            String name = splitCommand.get(1);
                            String age = splitCommand.get(2);
                            if (!NumberUtils.isParsable(age)) {
                                System.out.println("Age [" + age + "] is not a number!");
                                continue;
                            }

                            PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ONE);
                            ps.setString(1, name);
                            ps.setInt(2, Integer.parseInt(age));
                            int count = ps.executeUpdate();
                            System.out.println("Inserted [" + count + "] items;");
                            ps.close();
                        }
                    } else if (command.toLowerCase().startsWith("get ")) {
                        if (splitCommand.size() != 2) {
                            System.out.println("Invalid command...");
                        } else {
                            String id = splitCommand.get(1);
                            if (!NumberUtils.isParsable(id)) {
                                System.out.println("ID [" + id + "] is not a number!");
                                continue;
                            }

                            PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ONE);
                            ps.setInt(1, Integer.parseInt(id));
                            ResultSet rs = ps.executeQuery();
                            while (rs.next()) {
                                System.out.print(rs.getString(1));
                                System.out.print(", ");
                                System.out.print(rs.getString(2));
                                System.out.print(", ");
                                System.out.println(rs.getString(3));
                            }
                            rs.close();
                            ps.close();
                        }
                    } else if (command.toLowerCase().startsWith("exit")
                            && (splitCommand.size() == 1
                            && splitCommand.get(0).equalsIgnoreCase("exit"))) {
                        System.out.println("Bye...");
                        break;
                    } else {
                        System.out.println("Unknown command, you may use list/put/get/exit. Try again.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
