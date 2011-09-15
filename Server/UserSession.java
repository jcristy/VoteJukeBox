public class UserSession
{
	private static final long TIMEOUT = 20000;//20 seconds is considered enough time to have logged out
	private String ip;
	private long time;
	public UserSession(String ip, long time)
	{
		this.ip   = ip;
		this.time = time;
	}
	public void pinged()
	{
		time = System.currentTimeMillis();
	}
	public String getIP()
	{
		return ip;
	}
	public boolean checkTimeout()
	{
		return (System.currentTimeMillis()-TIMEOUT > time);
	}
}
