package Donald.apier;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Apier extends JavaPlugin implements Listener {

    private String latestChatMessage = "There aren't any chats.";
    private int playerCount = 0;


    @Override
    public void onEnable() {
        // イベント登録
        Bukkit.getPluginManager().registerEvents(this, this);

        try {
            startHttpServer();
        } catch (IOException e) {
            getLogger().severe("Failed to start HTTP server: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        if (latestChatMessage.contains(formattedTime)){
            //かさばったとき
            latestChatMessage = latestChatMessage +  "\n["+formattedTime +"CHAT]"+ removeAmpersandWithOneChar(event.getPlayer().getName() + ": " + event.getMessage());
        }else{
            latestChatMessage = "["+formattedTime +"CHAT]"+ removeAmpersandWithOneChar(event.getPlayer().getName() + ": " + event.getMessage());
        }
     }

    public static String removeAmpersandWithOneChar(String input) {
        return input.replaceAll("§.", "");
    }

    private void startHttpServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(25550), 0);
        server.createContext("/memory", this::handleMemoryRequest);
        server.createContext("/chat", this::handleChatRequest);
        server.createContext("/player/count", this::playerRQ);
        server.createContext("/player/names", this::PlayerName);
        server.createContext("/",this::handleRQ);
        server.setExecutor(null);
        server.start();
        getLogger().info("HTTP server started on port 25550");
    }


    private void handleRQ(HttpExchange exchange) throws IOException {
        String response = getHtml();

        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }


    // メモリ情報
    private void handleMemoryRequest(HttpExchange exchange) throws IOException {
        double ur = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        double tr = (double) Runtime.getRuntime().totalMemory() / (1024 * 1024);
        int MemoryUsage = (int) ((ur / tr) * 100);

        String response = String.valueOf(MemoryUsage);
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    // チャット情報
    private void handleChatRequest(HttpExchange exchange) throws IOException {
        String response = latestChatMessage;
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
    //プレイヤー数
    private void playerRQ(HttpExchange exchange) throws IOException {
        String response = String.valueOf(playerCount);
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private void PlayerName(HttpExchange exchange) throws IOException{
        String response = getPlayerNames();
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private String getPlayerNames(){
        String PlayerNames = "";
        if (Bukkit.getOnlinePlayers().isEmpty()){
            return "There are no online players.";
        }
        for (int i = 0; i < Bukkit.getOnlinePlayers().size(); i++) {
            PlayerNames = PlayerNames + ","+Bukkit.getOnlinePlayers().iterator().next().getName();
        }
        return PlayerNames;
    }
    @EventHandler
    private void onJoin(PlayerJoinEvent e){
       playerCount = Bukkit.getOnlinePlayers().size();
    }
    @EventHandler
    private void onQuit(PlayerQuitEvent e){
       playerCount = Bukkit.getOnlinePlayers().size();
    }
    private String getHtml() {
    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>APIer</title>
              <style>
                body {
                  margin: 0;
                  font-family: Arial, sans-serif;
                  background-color: #3E3E3E;
                  color: #ffffff;
                  overflow-x: hidden;
                }
            
                .menu-icon {
                position: fixed;
                top: 10px;
                left: 10px;
                width: 30px;
                height: 30px;
                cursor: pointer;
                padding: 1px;
                z-index: 1000;
                }
            
                .menu-icon div {
                  background-color: #ffffff;
                  height: 4px;
                  width: 100%;
                  margin: 5px 0;
                  transition: 0.3s;
                border-top: 20px;
                }
            
                .menu-icon.active div:nth-child(1) {
                  transform: rotate(45deg) translate(4px, -5px);
                  transform-origin: left center;
                }
            
                .menu-icon.active div:nth-child(2) {
                  opacity: 0;
                }
            
                .menu-icon.active div:nth-child(3) {
                  transform: rotate(-45deg) translate(2px, 5px);
                  transform-origin: left center;
                }
            
                .nav {
                  position: fixed;
                  top: 0;
                  left: -250px;
                  width: 250px;
                  height: 100%;
                  background-color: #1f1f1f;
                  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.5);
                  overflow: hidden;
                  transition: left 0.3s ease-in-out;
                  padding-top: 50px;
                }
            .start {
                margin-left: 30px; /* 左に10pxの余白を設定 */
            }
                .nav a {
                  display: block;
                  padding: 10px 20px;
                  text-decoration: none;
                  color: #ffffff;
                  border-bottom: 1px solid #333;
                  transition: background 0.3s;
                }
            
                .nav a:hover {
                  background-color: #333333;
                }
            
                .nav.active {
                  left: 0;
                }
            
                .top-container {
                  padding-top: 60px; /* 固定された要素を避けるための余白 */
                  text-align: center;
                }
            
                .text- {
                  width: 90%;
                  margin: 0 auto;
                  margin-top: 20px;
                margin-bottom:20px;
                  background-color: #2D2D2D;
                  border-radius: 10px;
                  padding: 16px;
                  color: #ffffff;
            
                }
                  .code-box{
                      border-radius: 15px;
                      background-color: #131313
                  }
            
                .top-title {
                margin-top: -45px;
                }
            
                pre {
                background-color: #111111;
                padding-top: 0px;
                padding-right: 15px;
                padding-left: 15px;
                border-radius: 10px;
                overflow-x: auto;
                color: #ffffff;
                font-family: 'Courier New', Courier, monospace;
                line-height: 1.5;
                }
            
                code {
                  font-size: 14px;
            
                }
                  footer{
                      background-color: rgba(40,40,40,0.8);
                      padding:1em;
                      text-align: center;
                      color:#B0B0B0;
                      font-size:0.875rem;
            
                  }
                  .codei{
                     background-color: #000000;font-size: 12px;
                  }
                  .normal{
                      font-size: 1em;
                  }
                  .top{
                      text-align: center;
            
                      overflow:hidden;
            
                  }
                  .discription{
                      font-size: 10px
                    ;color: #979797;
                  }
                  .top-sentence{
                      text-align: center;
                  }
              </style>
            </head>
            <body>
            <div class="menu-icon" id="menu-icon">
              <div></div>
              <div></div>
              <div></div>
            </div>
            
            <nav class="nav" id="nav">
              <li><a href="/memory">Memory <br> メモリ</a></li>
              <li><a href="/player/count">PlayerCount <br>プレイヤー数</a></li>
              <li><a href="/player/names">PlayerNames<br>プレイヤーの名前</a></li>
              <li><a href="/chat">Last chat in game<br>最終チャット</a></li>
            </nav>
            
            <div class="top-container">
              <h2 class="top-title">APIer Plugin!</h2>
              <hr>
              <h1>Hello There!</h1>
              <h3><strong>Welcome to APIer Plugin website.</strong></h3>
              <h3>You can view MC-Server Information from this website.</h3>
              <h3>Click on the three lines menu on the left!</h3>
              <h4>If you click on the menu and it doesn't work, please refresh the page.</h4>
              <br><br><br>
              <h1>やぁこんにちは！</h1>
              <h3><strong>ようこそ！APIer Pluginのウェブサイトへ</strong></h3>
              <h3>ここではマイクラ鯖の情報を閲覧できるよ</h3>
              <h3>左の三本線メニューをクリックして始めよう！</h3>
              <h4>メニューをクリックしても動かないなら再読み込みしてください。</h4>
            </div>
            
            <footer>
              <p>© 2024 APIer Minecraft Plugin</p>
            </footer>
            <style>
              .container {
                display: flex;
                justify-content: center;
                align-items: center;
                flex-direction: column;
              }
              .profile {
                      margin: 0;
                  border-radius: 15px;
                height: 250px;
                display: flex;
                justify-content: center;
                align-items: center;
                background-color: #333;
                display: flex;
                align-items: center;
              }
              .profile-image {
                width: 200px;
                border-radius: 50%;
                margin-right: 10px;
              }
              .profile-text {
                color: white;
                font-size: 25px;
                overflow-y: auto;
                height : 250px;
              }
            </style>
            <script>
              const menuIcon = document.getElementById('menu-icon');
              const nav = document.getElementById('nav');
            
              menuIcon.addEventListener('click', () => {
                menuIcon.classList.toggle('active');
                nav.classList.toggle('active');
              });
            </script>
            </body>
            </html>
            """;
    }

}
