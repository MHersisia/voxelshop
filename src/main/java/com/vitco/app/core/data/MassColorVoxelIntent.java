package com.vitco.app.core.data;

import java.awt.Color;

import com.vitco.app.core.data.history.VoxelActionIntent;

final class MassColorVoxelIntent extends VoxelActionIntent  {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer[] voxelIds;
    private final Color color;

    protected MassColorVoxelIntent(VoxelData voxelData, Integer[] voxelIds, Color color, boolean attach) {
        super(attach);
		this.voxelData = voxelData;

        // what is effected (there could be duplicate positions here)
        effected = new int[voxelIds.length][];
        for (int i = 0; i < effected.length; i++) {
            effected[i] = this.voxelData.dataContainer.voxels.get(voxelIds[i]).getPosAsInt();
        }

        this.voxelIds = voxelIds;
        this.color = color;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            for (Integer voxelId : voxelIds) {
                this.voxelData.historyManagerV.applyIntent(new ColorVoxelIntent(this.voxelData, voxelId, color, true));
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