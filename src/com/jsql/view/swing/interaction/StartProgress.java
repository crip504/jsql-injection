/*******************************************************************************
 * Copyhacked (H) 2012-2014.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.interaction;

import javax.swing.tree.DefaultTreeModel;

import com.jsql.model.bean.AbstractElementDatabase;
import com.jsql.view.swing.MediatorGUI;
import com.jsql.view.swing.tree.AbstractNodeModel;

/**
 * Start refreshing the progress bar of an element in the database tree.
 */
public class StartProgress implements IInteractionCommand {
    /**
     * The element in the database tree for which the progress starts.
     */
    private AbstractElementDatabase dataElementDatabase;

    /**
     * @param interactionParams Element in the database tree to update
     */
    public StartProgress(Object[] interactionParams) {
        dataElementDatabase = (AbstractElementDatabase) interactionParams[0];
    }

    @Override
    public void execute() {
        // Tree model, update the tree (refresh, add node, etc)
        DefaultTreeModel treeModel = (DefaultTreeModel) MediatorGUI.databaseTree().getModel();

        // Get the node
        AbstractNodeModel progressingTreeNodeModel =
                (AbstractNodeModel) MediatorGUI.gui().getTreeNodeModels().get(dataElementDatabase).getUserObject();
        // Mark the node model as 'display progress bar'
        progressingTreeNodeModel.hasProgress = true;

        // Update the node
        treeModel.nodeChanged(MediatorGUI.gui().getTreeNodeModels().get(dataElementDatabase));
    }
}
