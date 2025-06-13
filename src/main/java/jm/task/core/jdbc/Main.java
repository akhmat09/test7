package jm.task.core.jdbc;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.service.UserService;
import jm.task.core.jdbc.service.UserServiceImpl;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();


        userService.createUsersTable();


        userService.saveUser("Иван", "Иванов", (byte) 25);
        userService.saveUser("Петр", "Петров", (byte) 30);
        userService.saveUser("Сергей", "Сергеев", (byte) 22);
        userService.saveUser("Анна", "Кузнецова", (byte) 28);


        List<User> users = userService.getAllUsers();
        for (User user : users) {
            System.out.println(user);
        }


        userService.cleanUsersTable();


        userService.dropUsersTable();
    }
}
