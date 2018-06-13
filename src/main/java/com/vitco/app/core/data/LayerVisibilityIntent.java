package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class LayerVisibilityIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer layerId;
    private final boolean visible;
    private boolean oldVisible;

    protected LayerVisibilityIntent(VoxelData voxelData, int layerId, boolean visible, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.layerId = layerId;
        this.visible = visible;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            oldVisible = this.voxelData.dataContainer.layers.get(layerId).isVisible();
        }
        this.voxelData.dataContainer.layers.get(layerId).setVisible(visible);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.layers.get(layerId).setVisible(oldVisible);
    }

    private int[][] effected = null; // everything effected
    @Override
    public int[][] effected() {
        if (effected == null) { // get effected positions
            Voxel[] voxels = this.voxelData.dataContainer.layers.get(layerId).getVoxels();
            effected = new int[voxels.length][];
            for (int i = 0, voxelsLength = voxels.length; i < voxelsLength; i++) {
                effected[i] = voxels[i].getPosAsInt();
            }
        }
        return effected;
    }
}