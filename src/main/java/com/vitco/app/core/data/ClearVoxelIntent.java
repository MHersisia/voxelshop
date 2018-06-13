package com.vitco.app.core.data;

import java.util.ArrayList;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class ClearVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final int layerId;

    protected ClearVoxelIntent(VoxelData voxelData, int layerId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.layerId = layerId;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            ArrayList<int[]> effected = new ArrayList<int[]>();

            // get all voxels and remove them
            for (Voxel voxel : this.voxelData.dataContainer.layers.get(layerId).getVoxels()) {
                effected.add(voxel.getPosAsInt());
                this.voxelData.historyManagerV.applyIntent(new RemoveVoxelIntent(this.voxelData, voxel.id, true));
            }

            // what is effected
            this.effected = new int[effected.size()][];
            effected.toArray(this.effected);
        }
    }

    @Override
    protected void unapplyAction() {
        // nothing to do
    }

    private int[][] effected = null;
    @Override
    public int[][] effected() {
        return effected;
    }
}