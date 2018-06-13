package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class RemoveVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final int voxelId;
    private Voxel voxel;

    protected RemoveVoxelIntent(VoxelData voxelData, int voxelId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxelId = voxelId;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            voxel = this.voxelData.dataContainer.voxels.get(voxelId);
        }
        this.voxelData.dataContainer.voxels.remove(voxel.id);
        this.voxelData.dataContainer.layers.get(voxel.getLayerId()).removeVoxel(voxel);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.voxels.put(voxel.id, voxel);
        this.voxelData.dataContainer.layers.get(voxel.getLayerId()).addVoxel(voxel);
    }

    @Override
    public int[][] effected() {
        return new int[][]{voxel.getPosAsInt()};
    }
}