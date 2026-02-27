import React, { useEffect, useState } from 'react';
import { ProjectRole, useAuthStore } from '../../stores/useAuthStore';
import { useProjectStore } from '../../stores/useProjectStore';
import axios from 'axios';
import { Trash2, UserPlus, Shield, LogOut, ArrowRightLeft, Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface Member {
    id: string;
    user: {
        id: string;
        username: string;
        email: string;
    };
    role: ProjectRole;
    joinedAt: string;
}

interface Invitation {
    id: string;
    email: string;
    role: ProjectRole;
    status: string;
}

interface MembersTabProps {
    projectId: string;
}

export const MembersTab: React.FC<MembersTabProps> = ({ projectId }) => {
    const [members, setMembers] = useState<Member[]>([]);
    const [invitations, setInvitations] = useState<Invitation[]>([]);
    const [loading, setLoading] = useState(false);

    // Invitation Form
    const [inviteEmail, setInviteEmail] = useState('');
    const [inviteRole, setInviteRole] = useState<ProjectRole>(ProjectRole.VIEWER);
    const [error, setError] = useState('');
    const [successMsg, setSuccessMsg] = useState('');

    // Owner Transfer
    const [showTransfer, setShowTransfer] = useState(false);
    const [transferTargetId, setTransferTargetId] = useState('');

    const { canManageMembers } = useProjectStore();
    const { user } = useAuthStore();
    const canManage = canManageMembers();
    const navigate = useNavigate();

    const fetchMembers = async () => {
        try {
            const response = await axios.get(`/api/projects/${projectId}/members`);
            setMembers(response.data);
        } catch (e) {
            console.error("Failed to fetch members", e);
        }
    };

    const fetchInvitations = async () => {
        if (!canManage) return;
        try {
            const response = await axios.get(`/api/projects/${projectId}/invitations`);
            setInvitations(response.data);
        } catch (e) {
            console.error("Failed to fetch invitations", e);
        }
    };

    useEffect(() => {
        setLoading(true);
        Promise.all([fetchMembers(), fetchInvitations()]).finally(() => setLoading(false));
    }, [projectId, canManage]);

    const handleInvite = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setSuccessMsg('');
        try {
            await axios.post(`/api/projects/${projectId}/invitations`, {
                email: inviteEmail,
                role: inviteRole
            });
            setInviteEmail('');
            setSuccessMsg('Invitation sent successfully');
            fetchInvitations();
        } catch (err: any) {
            setError(err.response?.data || "Failed to send invitation");
        }
    };

    const handleCancelInvitation = async (invitationId: string) => {
        if (!confirm("Cancel this invitation?")) return;
        try {
            await axios.delete(`/api/projects/${projectId}/invitations/${invitationId}`);
            fetchInvitations();
        } catch (e) {
            alert("Failed to cancel invitation");
        }
    };

    const handleUpdateRole = async (userId: string, role: ProjectRole) => {
        try {
            await axios.put(`/api/projects/${projectId}/members/${userId}`, { role });
            fetchMembers();
        } catch (e) {
            alert("Failed to update role");
        }
    };

    const handleRemoveMember = async (userId: string) => {
        if (!confirm("Are you sure you want to remove this member?")) return;
        try {
            await axios.delete(`/api/projects/${projectId}/members/${userId}`);
            fetchMembers();
        } catch (err: any) {
            alert(err.response?.data || "Failed to remove member");
        }
    };

    const handleLeaveProject = async () => {
        if (!confirm("Are you sure you want to leave this project? This action cannot be undone.")) return;
        try {
            await axios.delete(`/api/projects/${projectId}/members/me`);
            navigate('/');
        } catch (err: any) {
            alert(err.response?.data || "Failed to leave project");
        }
    };

    const handleTransferOwnership = async () => {
        if (!transferTargetId) return;
        if (!confirm("DANGER: You are about to transfer ownership of this project. You will lose owner privileges. This cannot be undone. Proceed?")) return;

        try {
            await axios.post(`/api/projects/${projectId}/transfer-ownership`, {
                newOwnerId: transferTargetId
            });
            alert("Ownership transferred successfully.");
            window.location.reload(); // Reload to refresh permissions
        } catch (err: any) {
            alert(err.response?.data?.error || "Failed to transfer ownership");
        }
    };

    const admins = members.filter(m => m.role === ProjectRole.ADMIN && m.user.id !== user?.id);

    return (
        <div className="p-4 bg-gray-900 text-white h-full overflow-y-auto space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-xl font-bold flex items-center gap-2">
                    <Shield size={20} />
                    Project Members
                </h2>
                <button
                    onClick={handleLeaveProject}
                    className="text-red-400 hover:text-red-300 text-sm flex items-center gap-1 px-3 py-1 rounded hover:bg-red-900/20 border border-transparent hover:border-red-900/50 transition-colors"
                >
                    <LogOut size={16} /> Leave Project
                </button>
            </div>

            {/* Invite Section */}
            {canManage && (
                <div className="bg-gray-800 p-4 rounded border border-gray-700">
                    <h3 className="text-sm font-semibold mb-3 text-gray-300 flex items-center gap-2">
                        <UserPlus size={16} /> Invite Member
                    </h3>
                    <form onSubmit={handleInvite} className="flex gap-2">
                        <input
                            type="email"
                            placeholder="User Email"
                            value={inviteEmail}
                            onChange={e => setInviteEmail(e.target.value)}
                            className="flex-1 bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-blue-500"
                            required
                        />
                        <select
                            value={inviteRole}
                            onChange={e => setInviteRole(e.target.value as ProjectRole)}
                            className="bg-gray-700 border border-gray-600 rounded px-3 py-1.5 text-sm focus:outline-none focus:border-blue-500"
                        >
                            <option value={ProjectRole.VIEWER}>Viewer</option>
                            <option value={ProjectRole.MEMBER}>Member</option>
                            <option value={ProjectRole.ADMIN}>Admin</option>
                        </select>
                        <button
                            type="submit"
                            className="bg-blue-600 hover:bg-blue-500 text-white px-4 py-1.5 rounded text-sm font-medium transition-colors"
                        >
                            Invite
                        </button>
                    </form>
                    {error && <div className="text-red-400 text-xs mt-2">{error}</div>}
                    {successMsg && <div className="text-green-400 text-xs mt-2">{successMsg}</div>}
                </div>
            )}

            {/* Pending Invitations */}
            {canManage && invitations.length > 0 && (
                <div className="bg-gray-800 rounded border border-gray-700 overflow-hidden">
                    <div className="bg-gray-750 px-4 py-2 border-b border-gray-700 flex items-center gap-2">
                         <Clock size={16} className="text-yellow-500"/>
                         <h3 className="text-sm font-medium text-gray-300">Pending Invitations</h3>
                    </div>
                    <table className="w-full text-sm text-left">
                        <tbody className="divide-y divide-gray-700">
                            {invitations.map(inv => (
                                <tr key={inv.id} className="hover:bg-gray-750/50">
                                    <td className="px-4 py-2 text-gray-300">{inv.email}</td>
                                    <td className="px-4 py-2">
                                        <span className="text-xs px-2 py-0.5 rounded border border-gray-600 bg-gray-700 text-gray-300">{inv.role}</span>
                                    </td>
                                    <td className="px-4 py-2 text-right">
                                        <button
                                            onClick={() => handleCancelInvitation(inv.id)}
                                            className="text-red-400 hover:text-red-300 text-xs hover:underline"
                                        >
                                            Cancel
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Members List */}
            <div className="bg-gray-800 rounded border border-gray-700 overflow-hidden">
                <table className="w-full text-sm text-left">
                    <thead className="bg-gray-750 text-gray-400 font-medium border-b border-gray-700">
                        <tr>
                            <th className="px-4 py-2">User</th>
                            <th className="px-4 py-2">Role</th>
                            <th className="px-4 py-2">Joined</th>
                            {canManage && <th className="px-4 py-2 text-right">Actions</th>}
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-700">
                        {loading ? (
                            <tr><td colSpan={4} className="px-4 py-4 text-center text-gray-500">Loading...</td></tr>
                        ) : members.length === 0 ? (
                            <tr><td colSpan={4} className="px-4 py-4 text-center text-gray-500">No members found</td></tr>
                        ) : (
                            members.map(member => (
                                <tr key={member.id} className="hover:bg-gray-750/50">
                                    <td className="px-4 py-2">
                                        <div className="font-medium text-white">
                                            {member.user.username}
                                            {member.user.id === user?.id && <span className="text-gray-500 ml-1">(You)</span>}
                                        </div>
                                        <div className="text-gray-500 text-xs">{member.user.email}</div>
                                    </td>
                                    <td className="px-4 py-2">
                                        {canManage && member.user.id !== user?.id ? (
                                            <select
                                                value={member.role}
                                                onChange={(e) => handleUpdateRole(member.user.id, e.target.value as ProjectRole)}
                                                className="bg-gray-700 border-none rounded px-2 py-1 text-xs outline-none cursor-pointer hover:bg-gray-600"
                                            >
                                                <option value={ProjectRole.VIEWER}>Viewer</option>
                                                <option value={ProjectRole.MEMBER}>Member</option>
                                                <option value={ProjectRole.ADMIN}>Admin</option>
                                            </select>
                                        ) : (
                                            <span className={`px-2 py-0.5 rounded text-xs font-mono border ${
                                                member.role === ProjectRole.ADMIN ? 'bg-purple-900/30 text-purple-300 border-purple-800' :
                                                member.role === ProjectRole.MEMBER ? 'bg-blue-900/30 text-blue-300 border-blue-800' :
                                                'bg-gray-700 text-gray-300 border-gray-600'
                                            }`}>
                                                {member.role}
                                            </span>
                                        )}
                                    </td>
                                    <td className="px-4 py-2 text-gray-400 text-xs">
                                        {new Date(member.joinedAt).toLocaleDateString()}
                                    </td>
                                    {canManage && (
                                        <td className="px-4 py-2 text-right">
                                            {member.user.id !== user?.id && (
                                                <button
                                                    onClick={() => handleRemoveMember(member.user.id)}
                                                    className="text-red-500 hover:text-red-400 p-1 rounded hover:bg-gray-700 transition-colors"
                                                    title="Remove Member"
                                                >
                                                    <Trash2 size={16} />
                                                </button>
                                            )}
                                        </td>
                                    )}
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Ownership Transfer (Only visible to current owner/creator logic not strictly separate from Admin,
                but backend enforces owner-only transfer. We assume Admin UI includes this for now,
                but backend will reject if not actual owner. Ideally we check isOwner in store.) */}
            {canManage && (
                <div className="pt-4 border-t border-gray-800">
                    <button
                        onClick={() => setShowTransfer(!showTransfer)}
                        className="text-xs text-gray-500 hover:text-gray-300 flex items-center gap-1"
                    >
                        <ArrowRightLeft size={14} /> Transfer Ownership...
                    </button>

                    {showTransfer && (
                         <div className="mt-2 bg-red-900/10 border border-red-900/30 p-3 rounded">
                             <h4 className="text-sm font-bold text-red-400 mb-2">Danger Zone: Transfer Ownership</h4>
                             <p className="text-xs text-gray-400 mb-2">Transfer this project to another Admin. You will lose Owner status.</p>
                             <div className="flex gap-2">
                                 <select
                                     value={transferTargetId}
                                     onChange={(e) => setTransferTargetId(e.target.value)}
                                     className="bg-gray-800 border border-gray-700 rounded text-xs px-2 py-1 flex-1"
                                 >
                                     <option value="">Select new owner...</option>
                                     {admins.map(admin => (
                                         <option key={admin.user.id} value={admin.user.id}>
                                             {admin.user.username} ({admin.user.email})
                                         </option>
                                     ))}
                                 </select>
                                 <button
                                     onClick={handleTransferOwnership}
                                     disabled={!transferTargetId}
                                     className="bg-red-800 hover:bg-red-700 text-white text-xs px-3 py-1 rounded disabled:opacity-50"
                                 >
                                     Transfer
                                 </button>
                             </div>
                             {admins.length === 0 && <p className="text-xs text-red-500 mt-1">No other Admins available.</p>}
                         </div>
                    )}
                </div>
            )}
        </div>
    );
};
