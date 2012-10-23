--------------------------------------------------------------------------------
--  Handler.......... : onAuthenticationComplete
--  Author........... : Zachary Burke
--  Description...... : Called as a result of onLogin to let you know whether
--                      the authentication was a success.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function DropBoxAI.onAuthenticationComplete ( result )
--------------------------------------------------------------------------------
	
    this.mLoggedIn ( result )
    
	if(result)
    then
        log.message ( "DropBox login successful" )
    else
        log.message ( "DropBox login failed" )
    end
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
