import java.io.*;
import java.net.*;

public class QuizClient {
    private static String serverIP = "localhost"; // 기본 IP
    private static int nPort = 1234; // 기본 포트

    public static void main(String[] args) {
        String quiz;
        String sentence;
        String answer;
        // 파일에서 서버 설정을 읽어오는 메서드 호출
        readServerConfig("server_info.dat");

        try (Socket clientSocket = new Socket(serverIP, nPort);
             DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
             BufferedReader inFromServer = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()));
             BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in))) {

            while (true) {
                // 서버로부터 퀴즈 수신
                quiz = inFromServer.readLine();
                if (quiz != null && quiz.startsWith("Final score :")) {
                    System.out.println();
                    System.out.println(quiz);
                    break;
                }
                System.out.println("Quiz : " + quiz);

                // 입력 전송
                sentence = inFromUser.readLine();
                outToServer.writeBytes(sentence + '\n');

                // 서버로부터 결과 수신
                answer = inFromServer.readLine();
                System.out.println("FROM SERVER : " + answer);
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 서버 설정을 파일에서 읽어오는 메서드
    private static void readServerConfig(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            if (line != null) {
                String[] config = line.split(":");
                serverIP = config[0].trim();
                nPort = Integer.parseInt(config[1].trim());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Configuration file not found.");
        } catch (IOException e) {
            System.err.println("Error reading configuration file: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Invalid port");
        }
    }
}

