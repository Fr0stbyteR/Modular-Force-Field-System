package mffs.tileentity;

import java.util.Set;

import mffs.api.modules.IModule;
import universalelectricity.core.vector.Vector3;
import calclavia.lib.CalculationHelper;

/**
 * A thread that allows multi-threading calculation of projector fields.
 * 
 * @author Calclavia
 * 
 */
public class ProjectorCalculationThread extends Thread
{
	public interface IThreadCallBack
	{
		/**
		 * Called when the thread finishes the calculation.
		 */
		public void onThreadComplete();
	}

	private TileEntityForceFieldProjector projector;
	private IThreadCallBack callBack;

	public ProjectorCalculationThread(TileEntityForceFieldProjector projector)
	{
		this.projector = projector;
	}

	public ProjectorCalculationThread(TileEntityForceFieldProjector projector, IThreadCallBack callBack)
	{
		this(projector);
		this.callBack = callBack;
	}

	@Override
	public void run()
	{
		this.projector.isCalculating = true;

		try
		{

			Set<Vector3> newField = this.projector.getMode().getExteriorPoints(this.projector);

			Vector3 translation = this.projector.getTranslation();
			int rotationYaw = this.projector.getRotationYaw();
			int rotationPitch = this.projector.getRotationPitch();

			for (Vector3 position : newField)
			{
				if (rotationYaw != 0 || rotationPitch != 0)
				{
					CalculationHelper.rotateByAngle(position, rotationYaw, rotationPitch);
				}

				position.add(new Vector3(this.projector));
				position.add(translation);

				if (position.intY() <= this.projector.worldObj.getHeight())
				{
					this.projector.getCalculatedField().add(position.round());
				}
			}

			for (IModule module : this.projector.getModules(this.projector.getModuleSlots()))
			{
				module.onCalculate(this.projector, this.projector.getCalculatedField());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.projector.isCalculating = false;
		this.projector.isCalculated = true;

		if (this.callBack != null)
		{
			this.callBack.onThreadComplete();
		}
	}
}