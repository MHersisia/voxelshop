package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class AlphaVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final int voxelId;
    private final int newAlpha;
    private int oldAlpha;
    private Voxel voxel;

    protected AlphaVoxelIntent(VoxelData voxelData, int voxelId, int newAlpha, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxelId = voxelId;
        this.newAlpha = newAlpha;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            voxel = this.voxelData.dataContainer.voxels.get(voxelId);
            oldAlpha = voxel.getAlpha();
            // what is effected
            effected = new int[][]{voxel.getPosAsInt()};
        }
        this.voxelData.dataContainer.layers.get(voxel.getLayerId()).setVoxelAlpha(voxel, newAlpha);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.layers.get(voxel.getLayerId()).setVoxelAlpha(voxel, oldAlpha);
    }

    private int[][] effected = null;
    @Override
    public int[][] effected() {
        return effected;
    }
}