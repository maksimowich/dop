package ru.mephi.test.console_platformer;

import ru.mephi.test.console_platformer.enumeration.Action;
import ru.mephi.test.console_platformer.expetions.IllegalPointException;
import ru.mephi.test.console_platformer.expetions.InvalidRoute;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;


public class RoutePlanner {
    private char[][] map;
    private char[][] backUp;
    private final Path path;
    private final boolean isWork;
    private int width;
    private int height;
    private final RouteBuilder routeBuilder;

    /**
     * Inner class (внутренний класс), необходимый для создания маршрута.
     * Его задача - построить путь по командам консоли
     */
    private class RouteBuilder {
        private Point startPoint;
        private Point endPoint;


        private static Action dialog(Scanner scanner){
            System.out.println("Select the type of action");
            System.out.println("""
                Input action:
                Input W to  go up
                Input A to  go left
                Input D to  go right
                Input S to  go down""");

            return getAction(scanner);
        }

        /**
         * Метод необходим для получения действия (куда строим маршрут)
         * @param scanner - откуда читаем действие
         * @return возвращаем тип действия
         */
        private static Action getAction(Scanner scanner){
            System.out.print("Input action: ");
            String result;
            Action action = null;
            boolean flag = false;
            while (action==null){
                if(flag){
                    System.out.println("Repeat your action: ");
                }
                result = scanner.next().trim().toUpperCase(Locale.ROOT);
                action = switch (result){
                    case "W" -> Action.UP;
                    case "A" -> Action.LEFT;
                    case "D" -> Action.RIGHT;
                    case "S" -> Action.DOWN;
                    default -> null;
                };
                flag = true;
            }
            return action;
        }

        /**
         * В этом классе делаем необходимые проверки для построения шага маршрута
         * Если все ок, то маркируем маршрут 'X'
         * @param current - текущая позиция маршрута
         * @throws InvalidRoute - описывает некорректное построение маршрута
         */
        private void setStepOfRoute(Point current) throws InvalidRoute {
            if(checkPoint(current)){
                throw new InvalidRoute(String.format("the route is out of bounds: %s",current ));
            }
            if(isObstacle(current)){
                throw new InvalidRoute("the route encountered an obstacle");
            }
            if(map[current.y][current.x] == ('.')){
                map[current.y][current.x] = 'X';
            }else {
                if(!current.equals(endPoint)){
                    throw new  InvalidRoute("the route is looped");
                }
            }
        }

        /**
         * Основная функция построения маршрута
         * Пошагово строит путь от стартовой позиции до конечной
         * @throws InvalidRoute - Если построить маршрут нельзя
         */
        private void buildingTrip() throws InvalidRoute{
            Point current = startPoint;
            Scanner scanner = new Scanner(System.in);
            while (!current.equals(endPoint)){
                showMap();
                Action action = dialog(scanner);
                switch (action){
                    case UP -> current.move(current.x , current.y-1);
                    case LEFT -> current.move(current.x-1 , current.y);
                    case RIGHT -> current.move(current.x+1 , current.y);
                    case DOWN -> current.move(current.x , current.y+1);
                }
                setStepOfRoute(current);
                saveMap(path.toString(),map);
            }
            System.out.println("Your route is built");
        }
    }


    public RoutePlanner(Path path) {
        this.path = path;
        isWork = loadMap(path);
        setBackUp();
        routeBuilder = new RouteBuilder();
    }

    /**
     * В данной функции делаем бекап нашей карты (да clone не работает)
     */
    private  void setBackUp(){
        backUp = new char[height][width];
        for (int i = 0; i< height;i++){
            for (int j =0; j<width;j++){
                backUp[i][j] = map[i][j];
            }
        }
    }

    /**
     * Получаем integer координату
     * @param scanner -сканер
     * @param a1 - первая граница
     * @param a2 - вторая граница
     * @param msg - сообщение (какая координата)
     * @return возвращаем координату
     */
    private static Integer getInteger(Scanner scanner, Integer a1, Integer a2, String msg)
    {
        String result;
        int changed;
        while(true){
            System.out.printf("Input %s [%d-%d]: ",msg,a1,a2);
            result = scanner.next().trim();
            try {
                changed = Integer.parseInt(result);
                break;
            }catch (NumberFormatException exception)
            {
                System.out.println(exception.getMessage());
            }
            System.out.println("Please repeat your answer");
        }
      return changed;
    }

    /**
     * Отобразить текущую карту
     */
    private void showMap(){
        for (int i = 0; i< height;i++){
            for (int j =0; j<width;j++){
                System.out.print(map[i][j]+" ");
            }
            System.out.println();
        }
    }

    /**
     * Производит создание незанятой точки карты
     * Если точка вышла за пределы карты или мы создаем точку в препятствии - выдаем ошибку
     * @return созданную точку
     * @throws IllegalPointException - ошибка при создании точки карты
     */
    private Point getPoint() throws IllegalPointException{
        Scanner scanner  = new Scanner(System.in);
        System.out.println("Print point....");
        Point point =  new Point(
                getInteger(scanner,0,width-1,"X"),
                getInteger(scanner,0,height-1,"Y"));
        if (map[point.y][point.x] == '*'){
            throw new IllegalPointException(String.format("This point is already occupied: %s", point));
        }
        if(checkPoint(point)){
            throw new IllegalPointException(String.format("Point outside the map: %s", point));
        }
        if(isObstacle(point)){
            throw new IllegalPointException(String.format("This point is an obstacle: %s", point));
        }
        return point;
    }

    /**
     * Проверяем точку за выход за границы
     * @param point - точка
     * @return true если точка не валидна/ false если все ок
     */
    private  boolean checkPoint(Point point){
        return !((point.x < width) &&
                (point.x >= 0) &&
                (point.y >= 0) &&
                (point.y < height));
    }

    /**
     * Проверка на препятствие
     * @param point - точка
     * @return true если точка является препятствием / else если иначе
     */
    private  boolean isObstacle(Point point){
        return map[point.y][point.x] == ('@');
    }

    /**
     * Функция загрузки карты. Используем для инициализации карты в конструкторе
     * Поэтому ошибки ловим здесь
     * @param path - путь карты
     * @return true - если карта загружена/ false если нет
     */
    public boolean loadMap(Path path){
        boolean flag = true;
        try(Scanner scan = new Scanner(new File(path.toString()))){
            setSize(path.toString());
            for (int i = 0; i< height;i++){
                for (int j =0; j<width;j++){
                    map[i][j] = scan.next().charAt(0);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            flag= false;
        }
        return flag;
    }

    /**
     * Функция сохранения карты
     * @param path - путь до карты
     * @param map - карта которую надо сохранить ( может быть как backUp, так и map)
     * @return true - если карта сохранена/ false если нет
     */
    public boolean saveMap(String path, char[][] map){
        boolean flag = true;
        try(PrintWriter printWriter = new PrintWriter(path)){
            for (int i = 0; i< height;i++){
                for (int j =0; j<width;j++){
                    printWriter.write(map[i][j]+ " ");
                }
                printWriter.write("\n");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            flag= false;
        }
        return flag;
    }

    /**
     * Устанавливаем начальную и конечную позиции маршрута
     * В случае ошибки генерирования точки возвращаем исключение
     * @throws IllegalPointException ошибка создания точки карты
     */
    private void  setStartPoints() throws IllegalPointException{
        try {
            System.out.println("Set start point coordinate");
            Point point = getPoint();
            map[point.y][point.x] = '*';
            routeBuilder.startPoint = point;
            System.out.println("Set end point coordinate");
            point = getPoint();
            map[point.y][point.x] = '*';
            routeBuilder.endPoint = point;
        }catch (IllegalPointException exception){
            throw new IllegalPointException("Point has incorrect coordinate",exception);
        }
    }

    /**
     * В данной функции устанавливаем размеры карты
     * @param path - путь до карты
     * @throws FileNotFoundException если файла нет
     */
    private void setSize(String path) throws FileNotFoundException {
        try(Scanner scan = new Scanner(new File(path))){
            int countHeight = 0;
            int countWidth = 0;
            while (scan.hasNext()) {
                countWidth = scan.nextLine().split(" ").length;
                countHeight++;
            }
            width = countWidth;
            height = countHeight;
            map = new char[countHeight][countWidth];
        }
    }

    /**
     * Основная функция приложения
     * Если карта загружена, то с картой можно работать (иначе говорит о невозможности)
     * Затем обращаемся к нашему routeBuilder и запускаем процесс создания карты
     * Если все ок он нам сообщит если нет, то ловим ошибку и откатываем исходную карту
     */
    public void run(){
        if(isWork){
            try {
                System.out.println("Initial map");
                showMap();
                setStartPoints();
                routeBuilder.buildingTrip();
            }catch (IllegalPointException | InvalidRoute e){
                System.err.println("The application has failed");
                e.printStackTrace();
                System.out.println("Try do backUP");
                boolean flag;
                flag = saveMap(path.toString(),backUp);
                if(flag){
                    System.out.println("Back up completed");
                }else {
                    System.out.println("Back up completed with Error!!!!!");
                }
            }
        }else {
            System.out.println("Sorry we can't run application, probably error of load map");
        }

    }
    public static void main(String[] args) {
        RoutePlanner tripPlanner = new RoutePlanner(Path.of("src/main/resources/map1.txt"));
        tripPlanner.run();
    }
}
