package com.vitco.app.core.data;

import java.awt.Color;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class ColorVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel voxel;
    private final Color newColor;
    private final Color oldColor;

    protected ColorVoxelIntent(VoxelData voxelData, int voxelId, Color newColor, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxel = this.voxelData.dataContainer.voxels.get(voxelId);
        this.oldColor = voxel.getColor();
        this.newColor = newColor;
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