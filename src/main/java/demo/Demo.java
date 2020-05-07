package demo;


import java.sql.SQLException;

public class Demo {
    public static void main(String[] args) {
        try{
            Manager manager = Manager.getInstance();
            System.out.println("manager worked");

        } catch (SQLException e){
            e.printStackTrace();
        }


    }
}
