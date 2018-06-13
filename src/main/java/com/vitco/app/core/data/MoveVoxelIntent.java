package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class MoveVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final int voxelId;
    private final int[] newPos;

    protected MoveVoxelIntent(VoxelData voxelData, int voxelId, int[] newPos, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxelId = voxelId;
        this.newPos = newPos;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            Voxel voxel = this.voxelData.dataContainer.voxels.get(voxelId);
            this.voxelData.historyManagerV.applyIntent(new RemoveVoxelIntent(this.voxelData, voxelId, true));

            // remove if something is at new position in this layer
            Voxel toRemove = this.voxelData.dataContainer.layers.get(voxel.getLayerId()).search(newPos);
            if (toRemove != null) {
                this.voxelData.historyManagerV.applyIntent(new RemoveVoxelIntent(this.voxelData, toRemove.id, true));
            }

            // add the voxel at new position
            this.voxelData.historyManagerV.applyIntent(new AddVoxelIntent(this.voxelData, voxelId, newPos, voxel.getColor(), voxel.isSelected(), voxel.getTexture(), voxel.getLayerId(), true));

            // what is effected
            effected = new int[][]{voxel.getPosAsInt(), newPos};
        }
    }

    @Override
    protected void unapplyAction() {
        // nothing to do here
    }

    private int[][] effected = null;
    @Override
    public int[][] effected() {
        return effected;
    }
}