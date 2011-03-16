package rhogenwizard;

import rhogenwizard.OSValidator.OSType;

public class RhodesAdapter 
{
	private static final String winRhogenFileName = "rhogen.bat";
	private static final String unixRhogenFileName = "rhogen";
	
	private String m_rhogenExe = null;
	private SysCommandExecutor m_executor = new SysCommandExecutor();
	
	public RhodesAdapter()
	{
		if (OSValidator.OSType.WINDOWS == OSValidator.detect()) {
			 m_rhogenExe = winRhogenFileName;
		} 
		else {
			m_rhogenExe = unixRhogenFileName;
		}
	}
	
	public boolean generateApp(BuildInfoHolder holder) throws Exception
	{
		m_executor.setWorkingDirectory(holder.m_appDir);
		
		StringBuilder sb = new StringBuilder();
		sb.append(m_rhogenExe + " ");
		sb.append("app ");
		sb.append(holder.m_appName);
		sb.append(" " + holder.generateAttributeString());
		
		m_executor.runCommand(sb.toString());
		
		return true;		
	}
	
	public boolean generateModel(String workDir, String modelName, String modelParams) throws Exception
	{
		m_executor.setWorkingDirectory(workDir);
		
		StringBuilder sb = new StringBuilder();
		sb.append(m_rhogenExe + " ");
		sb.append("model ");
		sb.append(modelParams + " ");
		sb.append(modelName);
		
		m_executor.runCommand(sb.toString());
		
		return true;		
	}
}
