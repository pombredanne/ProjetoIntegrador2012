<?php

App::uses('AppController', 'Controller');

/**
 * Rrcs Controller
 *
 * @property Rrc $Rrc
 */
class RrcsController extends AppController {

    /**
     * index method
     *
     * @return void
     */
    public function index() {
        $this->Rrc->recursive = 0;
        $this->set('rrcs', $this->paginate());
    }

    /**
     * view method
     *
     * @param string $id
     * @return void
     */
    public function view($id = null) {
        $this->Rrc->id = $id;
        if (!$this->Rrc->exists()) {
            throw new NotFoundException(__('Invalid rrc'));
        }
        $this->set('rrc', $this->Rrc->read(null, $id));
    }

    /**
     * add method
     *
     * @return void
     */
    public function add() {
        if ($this->request->is('post')) {
            //$this->Rrc->create();
            date_default_timezone_set('Brazil/East');
            $this->request->data['Rrc']['dataCriacao'] = date('Y-m-d H:i:s');



            if ($this->Rrc->save($this->request->data)) {
                $this->Session->setFlash(__('The rrc has been saved'));
                // $this->redirect(array('action' => 'index'));
            } else {
                $this->Session->setFlash(__('The rrc could not be saved. Please, try again.'));
            }
        }
        $usuarios = $this->Rrc->Usuario->find('list');
        $this->set(compact('usuarios'));
    }

    public function salvarAnexo($arrayAnexo, $id) {
        $fileTmp = $arrayAnexo['tmp_name'];
        $fileName = $arrayAnexo['name'];
        $diretorioDestino = "anexos/" . $id . "/";

        // cria diretorio se nao existir
        mkdir($diretorioDestino);

        // apaga a anexos ja existentes se existirem
        foreach ($anexos = scandir("$diretorioDestino") as $arquivoParaDeletar) {
            @unlink($diretorioDestino . $arquivoParaDeletar);
        }

        $diretorioDestino = $diretorioDestino . $fileName;

        if ($resultado = move_uploaded_file($fileTmp, $diretorioDestino)) {
            debug($resultado);
            return $diretorioDestino;
        } else {
            return "";
        }
    }

    /**
     * edit method
     *
     * @param string $id
     * @return void
     */
    public function edit($id = null) {
        $this->Rrc->id = $id;
        if (!$this->Rrc->exists()) {
            throw new NotFoundException(__('Invalid rrc'));
        }
        if ($this->request->is('post') || $this->request->is('put')) {
            if ($this->Rrc->save($this->request->data)) {
                $this->Session->setFlash(__('The rrc has been saved'));
                $this->redirect(array('action' => 'index'));
            } else {
                $this->Session->setFlash(__('The rrc could not be saved. Please, try again.'));
            }
        } else {
            $this->request->data = $this->Rrc->read(null, $id);
        }
        $usuarios = $this->Rrc->Usuario->find('list');
        $this->set(compact('usuarios'));
    }

    public function addAnexo($id = null) {
        $this->Rrc->id = $id;
        if (!$this->Rrc->exists()) {
            throw new NotFoundException(__('Invalid rrc'));
        }
        if ($this->request->is('post') || $this->request->is('put')) {
            echo $this->request->data['Rrc']['anexo']['tmp_name'] . "<br />";
            echo $this->request->data['Rrc']['anexo']['name'] . "<br />";
            echo $this->request->data['Rrc']['anexo']['error'] . "<br />";

            $anexoSalvo = $this->salvarAnexo($this->request->data['Rrc']['anexo'], $id);

            $this->request->data['Rrc']['anexo'] = $anexoSalvo;


            if ($this->Rrc->save($this->request->data)) {
                $this->Session->setFlash(__('The rrc has been saved'));
                $this->redirect(array('action' => 'index'));
            } else {
                $this->Session->setFlash(__('The rrc could not be saved. Please, try again.'));
            }
        } else {
            $this->request->data = $this->Rrc->read(null, $id);
        }
        $this->set(compact('usuarios'));
        $this->set('rrc', $this->Rrc->read(null, $id));
    }

    /**
     * delete method
     *
     * @param string $id
     * @return void
     */
    public function delete($id = null) {
        if (!$this->request->is('post')) {
            throw new MethodNotAllowedException();
        }
        $this->Rrc->id = $id;
        if (!$this->Rrc->exists()) {
            throw new NotFoundException(__('Invalid rrc'));
        }
        if ($this->Rrc->delete()) {
            $this->Session->setFlash(__('Rrc deleted'));
            $this->redirect(array('action' => 'index'));
        }
        $this->Session->setFlash(__('Rrc was not deleted'));
        $this->redirect(array('action' => 'index'));
    }

}