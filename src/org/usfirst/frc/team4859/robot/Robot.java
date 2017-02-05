
package org.usfirst.frc.team4859.robot;

import edu.wpi.first.wpilibj.CameraServer;

import javax.swing.plaf.metal.MetalFileChooserUI.FilterComboBoxRenderer;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.vision.VisionThread;

import org.usfirst.frc.team4859.robot.commands.ExampleCommand;
import org.usfirst.frc.team4859.robot.subsystems.ExampleSubsystem;
import org.opencv.core.Rect;
import com.ctre.CANTalon;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.vision.VisionRunner;
import edu.wpi.first.wpilibj.vision.VisionThread;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {

	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	public static OI oi;
	
	private final Object imgLock = new Object();
	private double centerX = 0.0;
	private int filtSize = 0;
	private int findSize = 0; 
	private VisionThread visionThread;

	
	Command autonomousCommand;
	SendableChooser<Command> chooser = new SendableChooser<>();

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		oi = new OI();
		chooser.addDefault("Default Auto", new ExampleCommand());
		// chooser.addObject("My Auto", new MyAutoCommand());
		SmartDashboard.putData("Auto mode", chooser);
		
		/*
		UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
		visionThread = new VisionThread(camera, new DavidVision(), pipeline -> {
	        if (!pipeline.filterContoursOutput().isEmpty()) 
	        {
	            //Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
	            synchronized (imgLock) {
	            	filtSize = pipeline.filterContoursOutput().size();
	            	findSize = pipeline.findContoursOutput().size();
	            	centerX = (double) 0.0;
	                //centerX = r.x + (r.width / 2);
	            }
	        }
	    });
		
		*/
		//
		//CameraServer.getInstance().startAutomaticCapture();
		new Thread(() -> {
             UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
             camera.setResolution(640, 480);
             
             CvSink cvSink = CameraServer.getInstance().getVideo();
             CvSource outputStream = CameraServer.getInstance().putVideo("Blur", 640, 480);
             
             Mat source = new Mat();
             Mat output = new Mat();
             
             while(!Thread.interrupted()) {
                 cvSink.grabFrame(source);
                 RoboPipeline blur = new RoboPipeline();
                 blur.process(source);
 	            synchronized (imgLock) {
	            	filtSize = blur.filterContoursOutput().size();
	            	findSize = blur.findContoursOutput().size();
	            	centerX = (double) 0.0;
 	            }
                 //Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY);
                 outputStream.putFrame(blur.hsvThresholdOutput());
             }
         }).start();
         
	}

	/**
	 * This function is called once each time the robot enters Disabled mode.
	 * You can use it to reset any subsystem information you want to clear when
	 * the robot is disabled.
	 */
	@Override
	public void disabledInit() {

	}

	@Override
	public void disabledPeriodic() {
		Scheduler.getInstance().run();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString code to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons
	 * to the switch structure below with additional strings & commands.
	 */
	@Override
	public void autonomousInit() {
		autonomousCommand = chooser.getSelected();

		/*
		 * String autoSelected = SmartDashboard.getString("Auto Selector",
		 * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
		 * = new MyAutoCommand(); break; case "Default Auto": default:
		 * autonomousCommand = new ExampleCommand(); break; }
		 */

		// schedule the autonomous command (example)
		if (autonomousCommand != null)
			autonomousCommand.start();
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		double centerX;
		Scheduler.getInstance().run();
		synchronized (imgLock) {
			centerX = this.centerX;
		}
		SmartDashboard.putDouble("position", centerX);
	}

	@Override
	public void teleopInit() {
		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
		if (autonomousCommand != null)
			autonomousCommand.cancel();
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		Scheduler.getInstance().run();
		
		double centerX;
		int f1;
		int f2;
		Scheduler.getInstance().run();
		synchronized (imgLock) {
			centerX = this.centerX;
			f1 = filtSize;
			f2 = findSize;
		}
		SmartDashboard.putNumber("position", centerX);	
		SmartDashboard.putNumber("Filtered", f1);	
		SmartDashboard.putNumber("Find", f2);	

	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}
}
