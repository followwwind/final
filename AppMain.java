import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import javafx.scene.*;

public class AppMain extends Application{
  
    private int core = 10;
    private int add = 0;
    private Button startBtn = new Button("开始");
    private Button endBtn = new Button("结束");
    private Label urlLable = new Label("URL");
    private TextField urlField = new TextField();
    private Label initLable = new Label("初始并发数");
    private TextField initField = new TextField(String.valueOf(core));
    private Label addLable = new Label("追加并发数");
    private TextField addField = new TextField(String.valueOf(add));
    
    private TabPane tabPane = new TabPane();
    private Tab startTab = new Tab("开始");
    private Tab configTab = new Tab("配置");
    private Tab outTab = new Tab("输出");
    private TextArea outText = new TextArea();
    private Label outLabel = new Label("");
    private List<Long> timeList = new CopyOnWriteArrayList<>();
    private boolean sign = true;
    private ExecutorService service = Executors.newFixedThreadPool(10);
    private Object lock = new Object();
    
    public void start(Stage stage){
      initTab();
      Scene scene = new Scene(new StackPane(tabPane), 640, 480);
      stage.setScene(scene);
      stage.show();
    }
    
    public void initTab(){
      startTab.setClosable(false);
      configTab.setClosable(false);
      outTab.setClosable(false);
      tabPane.getTabs().addAll(startTab, configTab, outTab);
      initPanel();
    }
    
    public void initPanel(){
      HBox urlBox = new HBox(15);
      urlBox.setAlignment(Pos.CENTER);
      urlBox.setPadding(new Insets(60, 15, 15, 45));
      urlBox.getChildren().addAll(urlLable, urlField);
      
      HBox initBox = new HBox(15);
      initBox.setAlignment(Pos.CENTER);
      initBox.setPadding(new Insets(30, 15, 15, 15));
      initBox.getChildren().addAll(initLable, initField);
      
      HBox addBox = new HBox(15);
      addBox.setAlignment(Pos.CENTER);
      addBox.setPadding(new Insets(30, 15, 15, 15));
      addBox.getChildren().addAll(addLable, addField);
      
      Button saveBtn = new Button("保存");
      saveBtn.setOnMouseCliecked(new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event){
          core = Integer.valueOf(initField.getText().trim());
          add = Integer.valueOf(addField.getText().trim());
        }
      });
      HBox saveBox = new HBox(15);
      saveBox.setAlignment(Pos.CENTER);
      saveBtn.setPrefSize(100, 30);
      saveBox.setPadding(new Insets(30, 15, 15, 15));
      saveBox.getChildren().addAll(saveBtn);
      
      VBox startBox = new VBox(10);
      startBtn.setPerfSize(100, 30);
      startBtn.setOnMouseCliecked(new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event){
          Strin httpUrl = urlField.getText().trim();
          sign = true;
          outText.setText("");
          timeList.clear();
          test(httpUrl);
        }
      });
      endBtn.setOnMouseCliecked(new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event){
          sign = false;
        }
      });
      endBtn.setPerfSize(100, 30);
      startBox.setAlignment(Pos.CENTER);
      startBox.setPadding(new Insets(30, 15, 30, 15));
      startBox.getChildren().addAll(startBtn, endBtn);
      
      BorderPane pane = new BorderPane();
      pane.setCenter(outText);
      pane.setBottom(outLabel);
      outTab.setContent(pane);
    }
    
    public void test(String httpUrl){
      CountDownLatch countDownLatch = new CountDownLatch(core + add);
      for(int i = 0; i < (core + add); i++){
        if(!sign){
          break;
        }
        service.execute(() -> {
          http(httpUrl);
          countDownLatch.countDown();
        });
      }
      countDownLatch.await();
      long maxTime = timeList.stream().mapToLog(Long::valueOf).max().orElse(0L);
      long avgTime = (long)timeList.stream().mapToLog(Long::valueOf).average().orElse(0L);
      outLabel.setText("最大响应时间:" + maxTime + "ms,平均响应时间:" + avgTime + "ms");
    }
    
    public boolean http(String httpUrl){
      HttpURLConnection conn = null;
      long start = System.currentTimeMillies();
      try{
        URL url = new URL(httpUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectionTimeOut(10000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        if(conn.getResponseCode() == 200){
          long end = System.currentTimeMillies();
          long diff = end - start;
          timeList.add(diff);
          synchronized(lock){
            outText.appendText("");
          }
          return true
        }
      }catch(IOException e){
      }
    }finally{
      if(conn != null){
        conn.disconnect();
      }
    }
    
    public static void main(){
      launch();
    }
}
