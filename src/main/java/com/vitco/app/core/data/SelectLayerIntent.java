package com.vitco.app.core.data;

import com.vitco.app.core.data.history.VoxelActionIntent;

final class SelectLayerIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer newLayerId;
    private Integer oldLayerId;

    protected SelectLayerIntent(VoxelData voxelData, int newLayerId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.newLayerId = newLayerId;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            oldLayerId = this.voxelData.dataContainer.selectedLayer;
        }
        this.voxelData.dataContainer.selectedLayer = newLayerId;
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.selectedLayer = oldLayerId;
    }

    @Override
    public int[][] effected() {
        // nothing effected
        return new int[0][];
    }
}