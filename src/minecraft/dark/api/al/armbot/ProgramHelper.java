package dark.api.al.armbot;

import java.util.HashMap;

import net.minecraft.world.World;

import universalelectricity.core.vector.Vector3;

/** Basic class to handle a armbot like programs for any object that uses the IArmbot class
 *
 * @author DarkGuardsman */
public class ProgramHelper
{
    /** Current Program */
    protected IProgram program;
    protected IArmbot bot;
    /** Current task in program */
    protected IArmbotTask currentTask;
    /** Do we have a memory to store values */
    boolean hasMemory = false;
    boolean hasTaskBeenCalled = false, nextTask = false;
    /** Max memorySize */
    protected int memorySize = 0;
    /** Array of values to remember between commands */
    protected HashMap<String, Object> taskMemory = new HashMap<String, Object>();

    public ProgramHelper(IArmbot bot)
    {
        this.bot = bot;
    }

    /** Needs to be called by the armbot per tick.
     *
     * @return true if it is doing something */
    public boolean onUpdate(World world, Vector3 botLocation)
    {
        if (program != null)
        {
            if (this.currentTask == null || this.nextTask)
            {
                this.nextTask();
            }
            if (this.currentTask != null)
            {
                if (!this.hasTaskBeenCalled && !this.currentTask.onMethodCalled(world, botLocation, bot))
                {
                    this.nextTask = true;
                }
                else
                {
                    if (!this.currentTask.onUpdate())
                    {
                        this.nextTask = true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void nextTask()
    {
        this.currentTask = program.getNextTask();
        this.hasTaskBeenCalled = false;
        this.nextTask = false;
    }

    public ProgramHelper(int varableLimit)
    {
        if (varableLimit > 0)
        {
            this.memorySize = varableLimit;
            this.hasMemory = true;
        }
        else
        {
            this.taskMemory = null;
        }
    }

    public ProgramHelper setProgram(IProgram program)
    {
        this.program = program;
        this.onProgramChanged();
        return this;
    }

    public void onProgramChanged()
    {
        this.taskMemory.clear();
        if (this.program != null)
        {
            HashMap<String, Object> memory = this.program.getDeclairedVarables();
            if (memory.size() <= memorySize)
            {
                //TODO load in memory and throw error stopping machine if there is not enough
            }
            this.program.init();
        }
    }

    /** Gets the memory location */
    public Object getMemoryLocation(String var)
    {
        return this.taskMemory.get(var);
    }

    /** Sets the memory location at the give spot. */
    public boolean setMemoryLocation(String var, Object object)
    {
        if (var != null)
        {
            if (this.taskMemory.containsKey(var))
            {
                if (object == null)
                {
                    this.taskMemory.remove(var);
                }
                return this.addMemory(var, object);

            }
            else if (object != null && this.taskMemory.size() < this.memorySize)
            {
                return this.addMemory(var, object);
            }
        }
        return false;
    }

    protected boolean addMemory(String var, Object object)
    {
        //We don't want cheat methods to bypass the variable memory limit
        if (!(object instanceof java.util.Arrays && object instanceof java.util.ArrayList && object instanceof java.util.List && object instanceof java.util.Set))
        {
            return this.taskMemory.put(var, object) != null;
        }
        return false;
    }
}