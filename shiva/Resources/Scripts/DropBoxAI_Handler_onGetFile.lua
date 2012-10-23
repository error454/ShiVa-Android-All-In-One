--------------------------------------------------------------------------------
--  Handler.......... : onGetFile
--  Author........... : Zachary Burke
--  Description...... : Get the specified file from the Dropbox app folder.  The
--                      file will be copied to an accessible area on disk and
--                      the full path will be returned in onGetFileResult.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function DropBoxAI.onGetFile ( filename )
--------------------------------------------------------------------------------
	
	system.callClientFunction ( "onDropBoxGetFile", filename )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
