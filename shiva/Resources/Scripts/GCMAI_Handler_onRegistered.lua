--------------------------------------------------------------------------------
--  Handler.......... : onRegistered
--  Author........... : 
--  Description...... : 
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function GCMAI.onRegistered ( id )
--------------------------------------------------------------------------------
	
    this.sID ( id )
	log.message ( "GCM successfully registered, your device id is: "..id )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------