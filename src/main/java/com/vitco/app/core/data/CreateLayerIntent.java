package com.vitco.app.core.data;

import com.vitco.app.core.data.container.VoxelLayer;
import com.vitco.app.core.data.history.VoxelActionIntent;

// layer intents
final class CreateLayerIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer layerId;
    private final String layerName;

    protected CreateLayerIntent(VoxelData voxelData, int layerId, String layerName, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.layerId = layerId;
        this.layerName = layerName;
    }

    @Override
    protected void applyAction() {
        this.voxelData.dataContainer.layers.put(layerId, new VoxelLayer(layerId, layerName));
        this.voxelData.dataContainer.layerOrder.add(0, layerId);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.layers.remove(layerId);
        this.voxelData.dataContainer.layerOrder.remove(this.voxelData.dataContainer.layerOrder.lastIndexOf(layerId));
    }

    @Override
    public int[][] effected() {
        // nothing effected
        return new int[0][];
    }
}