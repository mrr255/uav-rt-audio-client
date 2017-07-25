import java.awt.*;       // Using AWT layouts

import java.awt.event.*;
import javax.swing.*;
import java.awt.event.*; // Using AWT event classes and listener interfaces
import javax.swing.*;    // Using Swing components and containers
import java.util.regex.Pattern;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// A Swing GUI application inherits from top-level container javax.swing.JFrame
@SuppressWarnings("serial")
public class AudioGUI extends JFrame {
   private JTextField tfIP;
   private JTextField tfFile;
   private JButton btnIP;
   private JButton btnCon;
   private JButton btnDis;
   private JButton btnStp;
   private JButton btnSav;
   private JButton btnOpen;
   private JButton btnPlay;
   private String iP = "192.168.0.100";
   private boolean isConnected = false;
   private JTextArea jTextArea;
   private MessageConsole mc;
   static AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
   private String logString;
    private String logPath;
   private PrintWriter out;
   private JPanel conInd;
   private JPanel synInd;
   private JPanel recInd;
   private Container cp;
   private String path = "."+File.separator+"flightData";
   private String lastFile;
   private String lastFilePath;
   private SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss_SSS");

   // Constructor to setup the GUI components and event handlers
   public AudioGUI() {
      // Retrieve the content-pane of the top-level container JFrame
      // All operations done on the content-pane
      cp = getContentPane();
      cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));   // The content-pane sets its layout

      ImageIcon img = new ImageIcon("./logo_blue.png");
      setIconImage(img.getImage());

      JPanel ip = new JPanel();
      ip.setLayout(new FlowLayout());
      cp.add(ip);
      btnIP = new JButton("Select IP");
      btnIP.addActionListener(new UpdateIPListener());
      ip.add(btnIP);
      tfIP = new JTextField(15);
      tfIP.setText("192.168.0.100");
      tfIP.setBorder(BorderFactory.createCompoundBorder(
        tfIP.getBorder(),
        BorderFactory.createEmptyBorder(3, 5, 4, 5)));
      tfIP.addActionListener(new UpdateIPListener());
      ip.add(tfIP);

      JPanel savp = new JPanel();
      savp.setLayout(new FlowLayout());
      cp.add(savp);
      btnSav = new JButton("Browse Folder");
      btnSav.addActionListener(new SavListener());
      savp.add(btnSav);
      btnOpen = new JButton("Open Folder");
      btnOpen.addActionListener(new OpenListener());
      savp.add(btnOpen);

      JPanel conp = new JPanel();
      conp.setLayout(new FlowLayout());
      cp.add(conp);
      btnCon = new JButton("Connect and Record");
      btnCon.addActionListener(new ConListener());
      conp.add(btnCon);

      conInd=new JPanel();
      conInd.setPreferredSize(new Dimension(15,15));
      conInd.setBackground(Color.red);
      conp.add(conInd);


      JPanel synp = new JPanel();
      synp.setLayout(new FlowLayout());
      cp.add(synp);
      synp.add(new JLabel("Time Synched"));
      synInd=new JPanel();
      synInd.setPreferredSize(new Dimension(15,15));
      synInd.setBackground(Color.red);
      synp.add(synInd);

      JPanel recp = new JPanel();
      recp.setLayout(new FlowLayout());
      cp.add(recp);
      recp.add(new JLabel("Recording"));
      recInd=new JPanel();
      recInd.setPreferredSize(new Dimension(15,15));
      recInd.setBackground(Color.red);
      recp.add(recInd);


      JPanel disp = new JPanel();
      disp.setLayout(new FlowLayout());
      cp.add(disp);
      btnDis = new JButton("Stop Recording and Disconnect");
      btnDis.addActionListener(new DisListener());
      disp.add(btnDis);

      JPanel playp = new JPanel();
      playp.setLayout(new FlowLayout());
      cp.add(playp);
      btnPlay = new JButton("Play Last Recording");
      btnPlay.addActionListener(new PlayListener());
      playp.add(btnPlay);
      tfFile = new JTextField(18);
      tfFile.setText("");
      tfFile.setEditable(false);
      tfFile.setBorder(BorderFactory.createCompoundBorder(
      tfFile.getBorder(),
      BorderFactory.createEmptyBorder(3, 5, 4, 5)));
      playp.add(tfFile);

      jTextArea = new JTextArea();
      jTextArea.setLineWrap(true);
      jTextArea.setWrapStyleWord(true);
      //cp.add(jTextArea);
      JScrollPane scroll = new JScrollPane( jTextArea );
      scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setPreferredSize(new Dimension(600, 300));
      cp.add(scroll);
      mc = new MessageConsole(jTextArea);
      mc.redirectOut();
      mc.redirectErr(Color.RED, null);
      //mc.setMessageLines(100);


      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit program if close-window button clicked
      setTitle("Audio GUI"); // "super" JFrame sets title
      setSize(400, 600);         // "super" JFrame sets initial size
      setVisible(true);          // "super" JFrame shows
   }

   // The entry main() method
   public static void main(String[] args) {
      // Run the GUI construction in the Event-Dispatching thread for thread-safety
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            new AudioGUI(); // Let the constructor do the job
         }
      });
   }
   private class UpdateIPListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent evt) {
        boolean b = Pattern.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", tfIP.getText());
        //boolean b = true;
        if(isConnected)
        {
          System.out.println("Can't Change IP while Connected!");
          tfIP.setText(iP);
        }
        else if (b)
        {
          iP = tfIP.getText();
          System.out.println("IP set to: " + iP);

        }
        else{
          System.out.println("IP not valid: " + tfIP.getText());
          tfIP.setText(iP);

        }

      }
   }

   private class SavListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent evt) {
        if(isConnected)return;
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File(path+File.separator+"..")); // start at application current directory
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setSelectedFile(new File(path));
        int returnVal = fc.showOpenDialog(cp);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
          path = fc.getSelectedFile().toString();
          System.out.println(fc.getSelectedFile());
        }
      }
   }

   private class OpenListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        try
        {
        Runtime.getRuntime().exec("explorer.exe /open," + path);
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    }

    private class PlayListener implements ActionListener {
       @Override
       public void actionPerformed(ActionEvent evt)
       {
         try
         {
           File f = new File(lastFilePath + lastFile);
           if(f.exists() && !f.isDirectory())
           {
             System.out.println("Opening: " + lastFilePath + lastFile);
             Runtime.getRuntime().exec("explorer.exe /open," + lastFilePath + lastFile);
           }
           else
           {
             System.out.println("File Doesn't Exist: " + lastFilePath + lastFile);
           }
         }
         catch(Exception e)
         {
           e.printStackTrace();
         }
       }
     }

   void updateStatus(JPanel p, boolean state)
   {
     if(state)p.setBackground(Color.green);
     else p.setBackground(Color.red);

     cp.repaint();
   }

   private class ConListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent evt) {
        System.out.println("Connecting!");

        new ConThread();
      }
   }

   private class ConThread extends Thread
   {

     ConThread()
      {
        super();
        if(!isConnected)
        {
          start();
        }
        else{
          System.out.println("Already Connected!");
          return;
        }

      }

      public void run()
      {
        try
       {
         int i = 0;

          do
          {
            //System.out.println("Client: reading from " + iP + " :6666");
            try (Socket socket = new Socket())
            {
                socket.connect(new InetSocketAddress(iP,6666),2000);
                if (socket.isConnected())
               {
                  isConnected = true;
                  updateStatus(conInd,true);
                  System.out.println("Connected!");
                  new StpThread();
                   play(socket);
               }
           }
           catch(ConnectException e)
           {
             System.out.println("Unable to Connect.");
           }
           catch (SocketException e)
           {
             if (!isConnected) {
               System.out.println("in the SocketException");
               System.out.println(e);

             }
             else
             {
               System.out.println("No Connection: Retrying.");
               updateStatus(conInd,false);
               updateStatus(recInd,false);
               updateStatus(synInd,false);
             }
           }//end catch SocketException
          }while(isConnected);
       }
       catch(InterruptedException e)
       {
          System.out.println("my thread interrupted");
       }
       catch(UnknownHostException e)
       {
         System.out.println(e);
       }
       catch(IOException e)
       {
         System.out.println(e);
       }
       catch(Exception e)
       {System.out.println("in the catchall");
         System.out.println(e);
       }
       //System.out.println("Disconnected Successfully");
       isConnected = false;
       updateStatus(conInd,false);
       updateStatus(synInd,false);
      }

      private synchronized void play(final Socket in) throws Exception {

          File log;
          FileWriter logWriter;
          Date startTime = new Date();
          try{
              logPath =  path + File.separator;
              logString = dateForm.format(startTime) + ".log";
							log = new File(logPath + logString);
							logWriter = new FileWriter(log);
						 }
						catch(Exception e)
						{
							new File(path).mkdirs();
              logPath =  path + File.separator;
              logString = dateForm.format(startTime) + ".log";
							log = new File(logPath + logString);
              logWriter = new FileWriter(log);
						}
  				logWriter.write(dateForm.format(startTime) +".dat\n");
          logWriter.flush();
  				logWriter.close();
  				File dstFile = new File(path + File.separator +  dateForm.format(startTime) + ".dat");
  				FileOutputStream archive = new FileOutputStream(dstFile);
          AudioInputStream ais = new AudioInputStream(in.getInputStream(),format , Long.MAX_VALUE);//AudioSystem.getAudioInputStream(in);
          byte[] buffer = new byte[1600*5];
          int count;
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
          SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
          speakers.open(format);
          speakers.start();

          boolean first = true;
          while (isConnected)//((count = ais.read(buffer)) != -1)
          {
          	count = ais.read(buffer);
          	if(count != -1)
          	{
              if(first){updateStatus(recInd,true);first = false;}
          	//System.out.println(count);
              speakers.write(buffer, 0, count);
              archive.write(buffer, 0, count);
          	}
          	else
          		{
                if(!isConnected)
                {
                  //speakers.drain();
                  updateStatus(recInd,false);
         	        speakers.close();
         	        archive.close();
         	        break;
                }
          		System.out.println("nope");

                    updateStatus(recInd,false);
          	        speakers.close();
          	        archive.close();
                    throw new SocketException("Should Repeat");

          		}
          }//end of while
          updateStatus(recInd,false);
          speakers.close();
          archive.close();
          lastFile = dateForm.format(startTime)+ ".wav";
          lastFilePath = logPath;
          new AudioProcess(new String[] {logString, logPath});
          tfFile.setText(lastFile);
        }

    }

    private class DisThread extends Thread
    {
      DisThread()
       {
         super();
         start();
       }

       public void run()
       {
         if(!isConnected)
         {
           System.out.println("Not Connected!");
         }
         else
         {
         System.out.println("Disconnecting!");
         out.println("COMMAND: QUIT");
         isConnected = false;
         updateStatus(conInd,true);

        }
       }
     }


   private class DisListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent evt) {
        DisThread th = new DisThread();
      }
   }

   private class StpThread extends Thread
   {
     StpThread()
      {
        super();
        start();
      }

      public void run()
      {
        if(!isConnected)
        {
          System.out.println("Not Connected, can't Stop!");

        }
        else
        {
          //System.out.println("trying to stop.");

        try (Socket socket = new Socket())
        {
            socket.connect(new InetSocketAddress(iP,6667),2000);
            if (socket.isConnected())
           {
             BufferedReader input =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out =
                new PrintWriter(socket.getOutputStream(), true);

            String answer = "";
            String pattern = "\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}-\\d{2}\\-\\d{2}_\\d{3}";
            Pattern r = Pattern.compile(pattern);

              while (isConnected) {
                  //System.out.println("looking!");
                  try {
                      if(input.ready())
                      {
                      answer = input.readLine();
                      System.out.println(answer);

                      Matcher m = r.matcher(answer);
                      if (m.find( ))
                      {
                      //System.out.println("FOUND A TIME!");
                      Date log = dateForm.parse(m.group(0));
                      Date current = new Date();
                      //System.out.println(dateForm.format(current));

                      long diff = current.getTime() - log.getTime();
                      long diffSeconds = diff / 1000 % 60;

                      //if(diffSeconds < (long)2)
                      if(true)
                      {
                        System.out.println("Time Synched Successfully");
                        updateStatus(synInd,true);
                      }
                      else
                      {
                        System.out.println("TIME NOT SYNCHRONIZED!!!");
                        //System.out.println("Off by: "+diffSeconds+ " Seconds.");
                      }

                      }
                      else if (answer.contains("Server Reconnected Successfully!")) {
                        System.out.println("Time Sync State Unknown.");
                        synInd.setBackground(Color.gray);
                      }
                    }

                  }
                  finally
                 {}
              }
              //System.out.println("Stopping!");
              //new DisThread();
           }
       }
       catch (Exception e)
       {
           System.out.println("in the Stop Thread Exception");
           System.out.println(e);

       }
      }
    }
  }


  private class StpListener implements ActionListener {
     @Override
     public void actionPerformed(ActionEvent evt) {
       StpThread th = new StpThread();
     }
  }




}
