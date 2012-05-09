<?php

$logger = new HMCLogger("Lock");
$lockFile = $GLOBALS['HMC_CLUSTER_PATH']."/lockfile";
$GLOBALS['fileHdl'] = 0;

function LockAcquire() {
  global $lockFile;
  global $logger;
//  $logger->log_debug("About to acquire lock for pid: " .
//    json_encode(posix_getpid()));
  $fileHdl = fopen($lockFile, "r");
  $GLOBALS['fileHdl'] = $fileHdl;
  $retval = flock($fileHdl, LOCK_EX);
//  $logger->log_debug("Acquired Lock Status for Process: "
//    . json_encode(posix_getpid()). " : ". json_encode($retval));
  return;
}

function LockRelease() {
  global $lockFile;
  global $logger;
//  $logger->log_debug("Trying to release lock Process: "
//    . json_encode(posix_getpid()));
  $fileHdl = $GLOBALS['fileHdl'];
  $retval = flock($fileHdl, LOCK_UN);
//  $logger->log_debug("Released Lock Status for Process: "
//    . json_encode(posix_getpid()). " : ". json_encode($retval));
}

?>