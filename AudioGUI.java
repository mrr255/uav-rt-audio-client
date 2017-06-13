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

// A Swing GUI application inherits from top-level container javax.swing.JFrame
@SuppressWarnings("serial")
public class AudioGUI extends JFrame {   // JFrame instead of Frame
   private JTextField tfIP;  // Use Swing's JTextField instead of AWT's TextField
   private JButton btnIP;    // Using Swing's JButton instead of AWT's Button
   private JButton btnCon;    // Using Swing's JButton instead of AWT's Button
   private JButton btnDis;    // Using Swing's JButton instead of AWT's Button
   private JButton btnStp;    // Using Swing's JButton instead of AWT's Button
   private String iP = "192.168.0.100";
   private boolean isConnected = false;
   private JTextArea jTextArea;
   private MessageConsole mc;
   static AudioFormat format = new AudioFormat(48000, 16, 1, true, true);

   // Constructor to setup the GUI components and event handlers
   public AudioGUI() {
      // Retrieve the content-pane of the top-level container JFrame
      // All operations done on the content-pane
      Container cp = getContentPane();
      cp.setLayout(new FlowLayout());   // The content-pane sets its layout

      cp.add(new JLabel("IP"));
      tfIP = new JTextField(15);
      tfIP.setText("192.168.0.100");
      tfIP.setBorder(BorderFactory.createCompoundBorder(
        tfIP.getBorder(),
        BorderFactory.createEmptyBorder(3, 5, 4, 5)));

      tfIP.addActionListener(new UpdateIPListener());
      cp.add(tfIP);


      btnIP = new JButton("Update");
      btnIP.addActionListener(new UpdateIPListener());
      cp.add(btnIP);


      btnCon = new JButton("Connect");
      btnCon.addActionListener(new ConListener());
      cp.add(btnCon);

      btnDis = new JButton("Disconnect");
      btnDis.addActionListener(new DisListener());
      cp.add(btnDis);

      btnStp = new JButton("Stop");
      btnStp.addActionListener(new StpListener());
      cp.add(btnStp);


      jTextArea = new JTextArea(10, 24);
      jTextArea.setWrapStyleWord(true);
      //cp.add(jTextArea);
      cp.add( new JScrollPane( jTextArea ) );
      mc = new MessageConsole(jTextArea);
      mc.redirectOut();
      mc.redirectErr(Color.RED, null);
      //mc.setMessageLines(100);


      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit program if close-window button clicked
      setTitle("Audio GUI"); // "super" JFrame sets title
      setSize(400, 300);         // "super" JFrame sets initial size
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
            try (Socket socket = new Socket(iP, 6666))
           {
                if (socket.isConnected())
               {
                  isConnected = true;
                  System.out.println("Connected!");
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
      }

      private synchronized void play(final Socket in) throws Exception {
  				Date startTime = new Date();
  				SimpleDateFormat dateForm = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
          File log;
          FileWriter logWriter;
          try{
							log = new File("." + File.separator + "flightData" + File.separator + "remote-"+ dateForm.format(startTime) + ".log");
							logWriter = new FileWriter(log);
						 }
						catch(Exception e)
						{
							new File("." + File.separator + "flightData").mkdirs();
              log = new File("." + File.separator + "flightData" + File.separator + "remote-"+ dateForm.format(startTime) + ".log");
    					logWriter = new FileWriter(log);
						}
  				logWriter.write("remote-" +dateForm.format(startTime) +".dat\n");
          logWriter.flush();
  				logWriter.close();
  				File dstFile = new File("." + File.separator + "flightData" + File.separator + "remote-" + dateForm.format(startTime) + ".dat");
  				FileOutputStream archive = new FileOutputStream(dstFile);
          AudioInputStream ais = new AudioInputStream(in.getInputStream(),format , Long.MAX_VALUE);//AudioSystem.getAudioInputStream(in);
          byte[] buffer = new byte[1600*5];
          int count;
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
          SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
          speakers.open(format);
          speakers.start();
          while (isConnected)//((count = ais.read(buffer)) != -1)
          {
          	count = ais.read(buffer);
          	if(count != -1)
          	{
          	//System.out.println(count);
              speakers.write(buffer, 0, count);
              archive.write(buffer, 0, count);
          	}
          	else
          		{
                if(!isConnected)
                {
                  //speakers.drain();
         	        speakers.close();
         	        archive.close();
         	        break;
                }
          		System.out.println("nope");

          			 //speakers.drain();
          	        speakers.close();
          	        archive.close();
                    throw new SocketException("Should Repeat");

          		}
          }//end of while
          speakers.close();
          archive.close();
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
         isConnected = false;
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
          System.out.println("trying to stop.");
        try (Socket socket = new Socket(iP, 6667))
        {
            if (socket.isConnected())
           {
              System.out.println("Stopping!");
              new DisThread();
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
