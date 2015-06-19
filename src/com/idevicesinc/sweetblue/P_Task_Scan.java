package com.idevicesinc.sweetblue;

import com.idevicesinc.sweetblue.PA_StateTracker.E_Intent;
import com.idevicesinc.sweetblue.utils.Interval;

class P_Task_Scan extends PA_Task_RequiresBleOn
{	
	static enum E_Mode
	{
		BLE, CLASSIC;
	}
	
	private E_Mode m_mode = null;
	
	//TODO
	private final boolean m_explicit = true;
	private final double m_scanTime;
	
	public P_Task_Scan(BleManager manager, I_StateListener listener, double scanTime)
	{
		super(manager, listener);
		
		m_scanTime = scanTime;
	}
	
	@Override protected double getInitialTimeout()
	{
		return m_scanTime;
	}
	
	@Override public void execute()
	{
		//--- DRK > Because scanning has to be done on a separate thread, isExecutable() can return true
		//---		but then by the time we get here it can be false. isExecutable() is currently not thread-safe
		//---		either, thus we're doing the manual check in the native stack. Before 5.0 the scan would just fail
		//---		so we'd fail as we do below, but Android 5.0 makes this an exception for at least some phones (OnePlus One (A0001)).
		if( !getManager().getNative().getAdapter().isEnabled() )
		{
			fail();

			return;
		}

		m_mode = getManager().startNativeScan(m_explicit ? E_Intent.INTENTIONAL : E_Intent.UNINTENTIONAL);
		
		if( m_mode == null )
		{
			fail();
		}
	}
	
	private double getMinimumScanTime()
	{
		return Interval.secs(getManager().m_config.idealMinScanTime);
	}
	
	@Override protected void update(double timeStep)
	{
		if( this.getState() == PE_TaskState.EXECUTING && getTimeout() == Interval.INFINITE.secs() )
		{
			if( getTotalTimeExecuting() >= getMinimumScanTime() && getQueue().getSize() > 0 )
			{
				selfInterrupt();
			}
			else if( m_mode == E_Mode.CLASSIC && getTotalTimeExecuting() >= BleManagerConfig.MAX_CLASSIC_SCAN_TIME )
			{
				selfInterrupt();
			}
		}
	}
	
	@Override public PE_TaskPriority getPriority()
	{
		return PE_TaskPriority.TRIVIAL;
	}
	
	public E_Mode getMode()
	{
		return m_mode;
	}
	
	@Override public boolean executeOnSeperateThread()
	{
		return true;
	}
	
	@Override public boolean isInterruptableBy(PA_Task otherTask)
	{
		if( otherTask instanceof P_Task_Read || otherTask instanceof P_Task_Write || otherTask instanceof P_Task_ReadRssi )
		{
			if( otherTask.getPriority().ordinal() > PE_TaskPriority.FOR_NORMAL_READS_WRITES.ordinal() )
			{
				return true;
			}
			else if( otherTask.getPriority().ordinal() >= this.getPriority().ordinal() )
			{
				//--- DRK > Not sure infinite timeout check really matters here.
				return this.getTotalTimeExecuting() >= getMinimumScanTime();
//				return getTimeout() == TIMEOUT_INFINITE && this.getTotalTimeExecuting() >= getManager().m_config.minimumScanTime;
			}
		}
		else
		{
			return otherTask.getPriority().ordinal() > this.getPriority().ordinal();
		}
		
		return super.isInterruptableBy(otherTask);
	}
	
	@Override public boolean isExplicit()
	{
		return m_explicit;
	}
	
	@Override protected BleTask getTaskType()
	{
		return null;
	}
}
