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

@SuppressWarnings("serial")
public class LogProcessor extends JFrame
{

  String path = "./";
  Container cp;
  LogProcessor()
  {
    cp = getContentPane();


    cp.setLayout(new FlowLayout());
    JButton btnSav = new JButton("Browse Folder");
    btnSav.addActionListener(new SavListener());
    cp.add(btnSav);
    JButton btnOpen = new JButton("Open Folder");
    btnOpen.addActionListener(new OpenListener());
    cp.add(btnOpen);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Exit program if close-window button clicked
    setTitle("Log Processor"); // "super" JFrame sets title
    setSize(400, 400);         // "super" JFrame sets initial size
    setVisible(true);          // "super" JFrame shows
  }

  private class SavListener implements ActionListener {
     @Override
     public void actionPerformed(ActionEvent evt) {

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

  // The entry main() method
  public static void main(String[] args) {
     // Run the GUI construction in the Event-Dispatching thread for thread-safety
     SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
           new LogProcessor(); // Let the constructor do the job
        }
     });
  }
}
