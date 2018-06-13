package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

// if the layerid is null the voxel layerId will be used,
// otherwise the provided layerid
// the voxel id is never used (!)
final class MassAddVoxelIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Voxel[] voxels;
    private final Integer layerId;

    protected MassAddVoxelIntent(VoxelData voxelData, Voxel[] voxels, Integer layerId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;

        // what is effected (there could be duplicate positions here)
        effected = new int[voxels.length][];
        for (int i = 0; i < effected.length; i++) {
            effected[i] = voxels[i].getPosAsInt();
        }

        this.voxels = voxels;
        this.layerId = layerId;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            boolean layerIdSet = layerId != null;
            for (Voxel voxel : voxels) {
                this.voxelData.historyManagerV.applyIntent(
                        new AddVoxelIntent(this.voxelData, this.voxelData.getFreeVoxelId(), voxel.getPosAsInt(),
                                voxel.getColor(), voxel.isSelected(), voxel.getTexture(), layerIdSet ? layerId : voxel.getLayerId(), true));
            }
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