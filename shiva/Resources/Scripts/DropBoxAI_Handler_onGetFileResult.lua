--------------------------------------------------------------------------------
--  Handler.......... : onGetFileResult
--  Author........... : Zachary Burke
--  Description...... : Called when onGetFile completes, you can now read this
--                      file like normal.
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
function DropBoxAI.onGetFileResult ( fullPathAndFilename )
--------------------------------------------------------------------------------
	
	log.message ( "onGetFileResult: The path to the file is: " .. fullPathAndFilename )
    --log.message ( "file content: " .. cache.getFileContentAsString ( fullPathAndFilename ) )
	
--------------------------------------------------------------------------------
end
--------------------------------------------------------------------------------
