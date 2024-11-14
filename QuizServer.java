import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizServer {
    private static List<String> questions = List.of("5 * 10", "2 * 5", "1 * 3");
    private static List<String> answers = List.of("50", "10", "3");
    private static String serverIP = "localhost"; // 기본 IP
    private static int nPort = 1234; // 기본 포트
    private static final int THREAD_SIZE = 5; // 동시 접속 클라이언트 수

    public static void main(String[] args) {
        // 파일에서 IP와 포트를 읽어오는 메서드 호출
        readServerConfig("server_info.dat");

        // 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_SIZE);

        try (ServerSocket welcomeSocket = new ServerSocket(nPort)) {
            System.out.println("Server start.. (port#=" + nPort + ")\n");
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("New client connected");
                // 스레드 풀
                executorService.execute(new ClientHandler(connectionSocket));
            }
        } catch (IOException e) {
            System.err.println("Error" + e.getMessage());
        } finally {
            executorService.shutdown();
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

    // Runnable 클래스
    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream())) {
                
                int score = 0;

                // 모든 퀴즈에 대해서 반복
                for (int i = 0; i < questions.size(); i++) {
                    String quiz = questions.get(i);
                    String answer = answers.get(i);

                    // 퀴즈 출력
                    outToClient.writeBytes(quiz + '\n');
                    // 클라이언트로부터 답변 수신
                    String clientAnswer = inFromClient.readLine();
                    System.out.println("FROM CLIENT: " + clientAnswer);

                    // 정답 확인 및 응답
                    if (clientAnswer != null && clientAnswer.equals(answer)) {
                        score++;
                        outToClient.writeBytes("Correct\n");
                    } else {
                        outToClient.writeBytes("Incorrect\n");
                    }
                }

                // 최종 점수 출력
                outToClient.writeBytes("Final score : " + score + '\n');
                socket.close();
            } catch (IOException e) {
                System.err.println("Error handling client connection: " + e.getMessage());
            }
        }
    }
}

