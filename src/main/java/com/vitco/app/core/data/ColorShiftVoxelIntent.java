package com.vitco.app.core.data;

import java.awt.Color;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;
import com.vitco.app.util.misc.ColorTools;

final class ColorShiftVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel voxel;
    private final Color newColor;
    private final Color oldColor;

    protected ColorShiftVoxelIntent(VoxelData voxelData, int voxelId, float[] hsbOffset, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxel = this.voxelData.dataContainer.voxels.get(voxelId);
        this.oldColor = voxel.getColor();
        float[] currentHSB = ColorTools.colorToHSB(this.oldColor);
        this.newColor = ColorTools.hsbToColor(new float[] {
                (currentHSB[0] + hsbOffset[0] + 2) % 1,
                Math.max(0, Math.min(1, currentHSB[1] + hsbOffset[1])),
                Math.max(0, Math.min(1, currentHSB[2] + hsbOffset[2]))
        });
        this.effected = new int[][]{voxel.getPosAsInt()};
    }

    @Override
    protected void applyAction() {
        voxel.setColor(newColor);
    }

    @Override
    protected void unapplyAction() {
        voxel.setColor(oldColor);
    }

    private int[][] effected = null;
    @Override
    public int[][] effected() {
        return effected;
    }
}