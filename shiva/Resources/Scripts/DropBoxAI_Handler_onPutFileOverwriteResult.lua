--------------------------------------------------------------------------------
--  Handler.......... : onPutFileOverwriteResult
--  Author........... : Zachary Burke
--  Description...... : This callback is triggered when onPutFileOverwrite has
--                      completed.  Here we are printing out the filename and
--                      number of bytes written.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function DropBoxAI.onPutFileOverwriteResult ( filename, bytesWritten )
--------------------------------------------------------------------------------
	
	log.message ( "File: " .. filename .. " was written to with : " .. bytesWritten .. " bytes" )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
