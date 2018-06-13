package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

// move to new layer
final class MigrateIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel[] voxels;

    protected MigrateIntent(VoxelData voxelData, Voxel[] voxels, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.voxels = voxels;
    }

    private Integer[] convertVoxelsToIdArray(Voxel[] voxels) {
        // what is effected (there *should* not be duplicate positions
        // as they are all moved to one new layer)
        effected = new int[voxels.length][];
        for (int i = 0; i < effected.length; i++) {
            effected[i] = voxels[i].getPosAsInt();
        }

        Integer[] voxelIds = new Integer[voxels.length];
        int i = 0;
        for (Voxel voxel : voxels) {
            voxelIds[i++] = voxel.id;
        }
        return voxelIds;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            // create a new layer
            int layerId = this.voxelData.getFreeLayerId();
            this.voxelData.historyManagerV.applyIntent(new CreateLayerIntent(this.voxelData, layerId, "Migrated", true));
            // remove all voxels
            this.voxelData.historyManagerV.applyIntent(
                    new MassRemoveVoxelIntent(this.voxelData, convertVoxelsToIdArray(voxels), true));
            // add all voxels to new layer
            this.voxelData.historyManagerV.applyIntent(new MassAddVoxelIntent(this.voxelData, voxels, layerId, true));
            // select the new layer
            this.voxelData.historyManagerV.applyIntent(new SelectLayerIntent(this.voxelData, layerId, true));
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