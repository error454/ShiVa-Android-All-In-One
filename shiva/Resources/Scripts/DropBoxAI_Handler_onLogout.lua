--------------------------------------------------------------------------------
--  Handler.......... : onLogout
--  Author........... : Zachary Burke
--  Description...... : Logout of Dropbox
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function DropBoxAI.onLogout (  )
--------------------------------------------------------------------------------
	
	system.callClientFunction ( "onDropBoxLogout" ) 
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
