package com.vitco.app.core.data;

import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.core.data.container.VoxelLayer;
import com.vitco.app.core.data.history.VoxelActionIntent;

final class DeleteLayerIntent extends VoxelActionIntent {
    /**
	 * 
	 */
	private final VoxelData voxelData;
	private final Integer layerId;
    private Integer layerPosition;
    private String layerName;

    protected DeleteLayerIntent(VoxelData voxelData, int layerId, boolean attach) {
        super(attach);
		this.voxelData = voxelData;
        this.layerId = layerId;
    }

    @Override
    protected void applyAction() {
        if (isFirstCall()) {
            // remember effected positions
            VoxelLayer voxelLayer = this.voxelData.dataContainer.layers.get(layerId);
            
			effected = new int[voxelLayer.getVoxels().length][];
			
            // remove all points in this layer
            Voxel[] voxels = voxelLayer.getVoxels();
            for (int i = 0; i < voxels.length; i++) {
                Voxel voxel = voxels[i];
                this.voxelData.historyManagerV.applyIntent(new RemoveVoxelIntent(this.voxelData, voxel.id, true));
                effected[i] = voxel.getPosAsInt(); // store
            }
            // remember the position of this layer
            layerPosition = this.voxelData.dataContainer.layerOrder.indexOf(layerId);
            // and the name
            layerName = voxelLayer.getName();
        }
        this.voxelData.dataContainer.layers.remove(layerId);
        this.voxelData.dataContainer.layerOrder.remove(layerId);
    }

    @Override
    protected void unapplyAction() {
        this.voxelData.dataContainer.layers.put(layerId, new VoxelLayer(layerId, layerName));
        this.voxelData.dataContainer.layerOrder.add(layerPosition, layerId);
    }

    private int[][] effected = null; // everything effected
    @Override
    public int[][] effected() {
        return effected;
    }
}