--------------------------------------------------------------------------------
--  Handler.......... : onInit
--  Author........... : 
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function userAI.onInit (  )
--------------------------------------------------------------------------------
	
	application.setCurrentUserScene ( "squareRoom" )
    
    this.hEmitter ( scene.createRuntimeObject ( application.getCurrentUserScene ( ), "emitter" ) )
    
    object.setTranslation ( this.hEmitter ( ), 0, 2, 0, object.kGlobalSpace)
    
    hud.newTemplateInstance ( this.getUser ( ), "AAIODriver", "menu" )
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
