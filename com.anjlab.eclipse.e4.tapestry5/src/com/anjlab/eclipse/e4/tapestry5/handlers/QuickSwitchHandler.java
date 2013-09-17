package com.anjlab.eclipse.e4.tapestry5.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.BasicPartList;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.internal.PartPane;

import com.anjlab.eclipse.tapestry5.EclipseUtils;
import com.anjlab.eclipse.tapestry5.TapestryContext;
import com.anjlab.eclipse.tapestry5.views.ViewLabelProvider;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class QuickSwitchHandler extends AbstractHandler
{
    /**
     * The constructor.
     */
    public QuickSwitchHandler()
    {
    }

    private TapestryContext previousContext;
    
    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     */
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        
        IWorkbenchPartReference partReference = window.getActivePage().getActivePartReference();
        
        if (!(partReference instanceof EditorReference))
        {
            //  Make in only work in editors
            return null;
        }
        
        IFile file = EclipseUtils.getFileFromPage(window.getActivePage());
        
        if (file == null)
        {
            return null;
        }
        
        TapestryContext context = null;
        
        if (previousContext != null)
        {
            if (previousContext.contains(file))
            {
                context = previousContext;
            }
        }
        
        if (context == null)
        {
            context = new TapestryContext(file);
            
            previousContext = context;
        }
        
        if (context.getFiles().size() == 0)
        {
            return null;
        }
        
        final MElementContainerImpl mElementContainerImpl = new MElementContainerImpl(context);
        
        final ViewLabelProvider labelProvider = new ViewLabelProvider();
        
        final BasicPartList editorList = new BasicPartList(window.getShell(),
                SWT.ON_TOP, SWT.V_SCROLL | SWT.H_SCROLL,
                new EPartServiceImpl(window),
                mElementContainerImpl,
                new ISWTResourceUtilities()
                {
                    @Override
                    public ImageDescriptor imageDescriptorFromURI(URI uri)
                    {
                        IFile file = mElementContainerImpl.lookupFile(uri.toString());
                        
                        return labelProvider.getImageDescriptor(file);
                    }
                },
                false);
        
        editorList.setInput();
        
        Point size = editorList.computeSizeHint();
        editorList.setSize(size.x, size.y);
        
        Point centerPoint = getLocation(window, size);
        
        editorList.setLocation(centerPoint);

        editorList.setVisible(true);
        editorList.setFocus();
        editorList.getShell().addListener(SWT.Deactivate, new Listener() {
            public void handleEvent(org.eclipse.swt.widgets.Event event) {
                editorList.getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        editorList.dispose();
                    }
                });
            }
        });
        
        return null;
    }

    private Point getLocation(IWorkbenchWindow window, Point size)
    {
        Point centerPoint = null;
        
        IWorkbenchPartReference partReference = window.getActivePage().getActivePartReference();
        
        if (partReference instanceof EditorReference)
        {
            PartPane pane = ((EditorReference) partReference).getPane();
            Control control = pane.getControl();
            Rectangle partBounds = control.getBounds();
            
            centerPoint = new Point(partBounds.x, partBounds.y);
            
            alignPoint(centerPoint, control.getParent(), true, true);
            
            centerPoint.x += partBounds.width / 2;
            centerPoint.y += partBounds.height / 2;
        }
        
        if (centerPoint == null)
        {
            Monitor mon = window.getShell().getMonitor();
            
            Rectangle bounds = mon.getClientArea();
            
            Point screenCenter = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
            
            centerPoint = screenCenter;
        }
        
        centerPoint.x -= size.x / 2;
        centerPoint.y -= size.y / 2;
        
        return centerPoint;
    }

    private Composite alignPoint(Point centerPoint, Composite parent, boolean left, boolean top)
    {
        while (parent != null)
        {
            if (left)
            {
                centerPoint.x += parent.getBounds().x;
            }
            
            if (top)
            {
                centerPoint.y += parent.getBounds().y;
            }
            
            parent = parent.getParent();
        }
        return parent;
    }
}
