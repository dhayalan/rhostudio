package rhogenwizard;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.console.MessageConsoleStream;

class RhodesLogAdapter implements ILogDevice
{
	MessageConsoleStream m_consoleStream = ConsoleHelper.getConsoleMsgStream();

	@Override
	public void log(String str) 
	{
		if (null != m_consoleStream)
		{
			m_consoleStream.println(prepareString(str));
		}
	}
	
	private String prepareString(String message)
	{
		message = message.replaceAll("\\p{Cntrl}", " ");  		
		return message;
	}
}

public class RhodesAdapter 
{
	public enum EPlatformType
	{
		eWm,
		eAndroid,
		eBb,
		eIPhone,
		eWp7,
		eEmu,
		eUnknown
	};
	
	private static final String winRhogenFileName = "rhodes.bat";
	private static final String unixRhogenFileName = "rhodes";
	private static final String winRakeFileName = "rake.bat";
	private static final String unixRakeFileName = "rake";
		
	public static final String platformWinMobile = "wm";
	public static final String platformAdroid = "android";
	public static final String platformBlackBerry = "bb";
	public static final String platformIPhone = "iphone";
	public static final String platformWp7 = "wp";
	public static final String platformEmu = "win32:rhosimulator";
	
	private String m_rhogenExe = null; 
	private String m_rakeExe = null;
	private SysCommandExecutor m_executor = new SysCommandExecutor();
	
	public RhodesAdapter()
	{
		m_executor.setOutputLogDevice(new RhodesLogAdapter());
		m_executor.setErrorLogDevice(new RhodesLogAdapter());
		
		if (OSValidator.OSType.WINDOWS == OSValidator.detect()) 
		{
			 m_rakeExe   = winRakeFileName;   
			 m_rhogenExe = winRhogenFileName;
		} 
		else
		{
			m_rakeExe   = unixRakeFileName;
			m_rhogenExe = unixRhogenFileName;
		}
	}
	
	public int generateApp(BuildInfoHolder holder) throws Exception
	{
		m_executor.setWorkingDirectory(holder.getProjectLocationPath().toOSString());
		
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(m_rhogenExe);
		cmdLine.add("app");
		cmdLine.add(holder.appName);
		
		return m_executor.runCommand(cmdLine);
	}
	
	public int generateModel(String workDir, String modelName, String modelParams) throws Exception
	{
		m_executor.setWorkingDirectory(workDir);
		
		modelParams = prepareModelAttributes(modelParams);
		
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(m_rhogenExe);
		cmdLine.add("model");
		cmdLine.add(modelName);
		cmdLine.add(modelParams);
		
		return m_executor.runCommand(cmdLine);
	}
	
	public IProcess debugApp(String workDir, EPlatformType platformType, ILaunch launch, boolean onDevice) throws Exception
	{
		String platformName = convertDescFromPlatform(platformType);
		
		StringBuilder sb = new StringBuilder();
		sb.append("run:");
		sb.append(platformName);
		
		if (onDevice)
		{
			sb.append(":device");
		}

		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(m_rakeExe);
		cmdLine.add(sb.toString());
		
		String[] commandLine = new String[cmdLine.size()]; // {"rake.bat" , "run:win32:rhosimulator"};
		commandLine = cmdLine.toArray(commandLine);
		
		Process process = DebugPlugin.exec(commandLine, new File(workDir));

		return DebugPlugin.newProcess(launch, process, "rhodes-emu");
	}
	
	public int buildApp(String workDir, EPlatformType platformType, boolean onDevice) throws Exception
	{
		String platformName = convertDescFromPlatform(platformType);
		
		StringBuilder sb = new StringBuilder();
		sb.append("run:");
		sb.append(platformName);
		
		if (onDevice)
		{
			sb.append(":device");
		}
		
		m_executor.setWorkingDirectory(workDir);
		
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(m_rakeExe);
		cmdLine.add(sb.toString());
		
		return m_executor.runCommand(cmdLine);
	}
	
	private String prepareModelAttributes(String modelAttr)
	{
		StringBuilder   sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(modelAttr, ",");
		
		while (st.hasMoreTokens()) 
		{
			String token = st.nextToken();
			
			token = token.trim();
			token = token.replace(' ', '_');
			
			sb.append(token);
			
			if (st.hasMoreTokens())
			{
				sb.append(",");
			}
		}
		
		return sb.toString();
	}
	
	public static EPlatformType convertPlatformFromDesc(String plDesc)
	{
		if (plDesc.equals(platformWinMobile))
		{
			return EPlatformType.eWm;
		}
		else if (plDesc.equals(platformAdroid))
		{
			return EPlatformType.eAndroid;
		}
		else if (plDesc.equals(platformBlackBerry))
		{
			return EPlatformType.eBb;
		}
		else if (plDesc.equals(platformIPhone))
		{
			return EPlatformType.eIPhone;
		}
		else if (plDesc.equals(platformWp7))
		{
			return EPlatformType.eWp7;
		}
		else if (plDesc.equals(platformEmu))
		{
			return EPlatformType.eEmu;
		}
		
		return EPlatformType.eUnknown;
	}
	
	public static String convertDescFromPlatform(EPlatformType plType)
	{
		switch(plType)
		{
		case eWm:
			return platformWinMobile;
		case eAndroid:
			return platformAdroid;
		case eBb:
			return platformBlackBerry;
		case eIPhone:
			return platformIPhone;
		case eWp7:
			return platformWp7;
		case eEmu:
			return platformEmu;
		}

		return null;
	}
	
	public String runRakeTask(String workDir, String taskName) throws Exception
	{
		m_executor.setWorkingDirectory(workDir);
		
		List<String> cmdLine = new ArrayList<String>();
		cmdLine.add(m_rakeExe);
		cmdLine.add(taskName);
		
		m_executor.runCommand(cmdLine);
		
		return m_executor.getCommandOutput();
	}

	public void cleanApp(String workDir) throws Exception 
	{
		String cleanCmd = "clean:";

		runRakeTask(workDir, cleanCmd + platformWinMobile);
		runRakeTask(workDir, cleanCmd + platformAdroid);
		runRakeTask(workDir, cleanCmd + platformBlackBerry);
		runRakeTask(workDir, cleanCmd + platformIPhone);
		runRakeTask(workDir, cleanCmd + platformWp7);
	}
	
	public void cleanPlatform(String workDir, EPlatformType type) throws Exception
	{
		String cleanCmd = "clean:";
		String platformName = convertDescFromPlatform(type);
		
		runRakeTask(workDir, cleanCmd + platformName);
	}
}
